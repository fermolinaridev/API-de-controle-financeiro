package com.fernando.financas.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fernando.financas.dto.HistoricoResponse.PontoHistorico;
import com.fernando.financas.exception.ServicoIndisponivelException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Série histórica de preço (gráfico) via mfinance.com.br — API brasileira
 * pública e <b>sem chave</b>. Cobre ações ({@code /stocks}) e FIIs ({@code /fiis}).
 *
 * Fornece ~3 meses de histórico diário (a API não expõe janelas maiores).
 */
@Component
@Slf4j
public class MfinanceClient {

    private final RestClient http;
    private final Cache cache = new Cache(Duration.ofSeconds(60));

    public MfinanceClient(RestClient.Builder builder) {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) Duration.ofSeconds(5).toMillis());
        factory.setReadTimeout((int) Duration.ofSeconds(10).toMillis());
        this.http = builder
                .baseUrl("https://mfinance.com.br/api/v1")
                .requestFactory(factory)
                .build();
    }

    /** Histórico de fechamento de um ativo da B3 (tenta ação; se vazio, tenta FII). */
    public List<PontoHistorico> historico(String ticker) {
        String alvo = ticker.trim().toUpperCase();
        List<PontoHistorico> pontos = cache.getOuCarrega(alvo, () -> {
            List<PontoHistorico> acao = mapear(get("/stocks/historicals/{t}", alvo));
            if (!acao.isEmpty()) return acao;
            return mapear(get("/fiis/historicals/{t}", alvo));
        });
        if (pontos.isEmpty()) {
            throw new ServicoIndisponivelException("Histórico indisponível para este ativo");
        }
        return pontos;
    }

    private List<PontoHistorico> mapear(JsonNode body) {
        JsonNode serie = body.path("historicals");
        if (!serie.isArray()) return List.of();
        List<PontoHistorico> pontos = new ArrayList<>();
        for (JsonNode ponto : serie) {
            JsonNode fechamento = ponto.get("close");
            JsonNode data = ponto.get("date");
            if (fechamento == null || fechamento.isNull() || data == null || data.isNull()) continue;
            pontos.add(new PontoHistorico(
                    LocalDate.parse(data.asText().substring(0, 10)),
                    new BigDecimal(fechamento.asText())));
        }
        pontos.sort((a, b) -> a.data().compareTo(b.data()));
        return pontos;
    }

    private JsonNode get(String uriTemplate, String ticker) {
        try {
            JsonNode body = http.get()
                    .uri(uriTemplate, Map.of("t", ticker))
                    .retrieve()
                    .body(JsonNode.class);
            return body != null ? body : vazio();
        } catch (RestClientResponseException e) {
            // 404 = ativo não é desta categoria (ex.: FII chamado em /stocks) — deixa o fallback seguir
            if (e.getStatusCode().value() == 404) return vazio();
            log.warn("mfinance respondeu {} para {}", e.getStatusCode().value(), ticker);
            throw new ServicoIndisponivelException(
                    "Gráfico temporariamente indisponível (fonte externa). Tente novamente em instantes.");
        } catch (RestClientException e) {
            log.warn("Falha ao consultar mfinance ({}): {}", ticker, e.getMessage());
            throw new ServicoIndisponivelException(
                    "Gráfico temporariamente indisponível (fonte externa). Tente novamente em instantes.");
        }
    }

    private static JsonNode vazio() {
        return com.fasterxml.jackson.databind.node.JsonNodeFactory.instance.objectNode();
    }

    private static final class Cache {
        private record Entry(List<PontoHistorico> value, long expiresAt) {}

        private final ConcurrentHashMap<String, Entry> map = new ConcurrentHashMap<>();
        private final long ttlMillis;

        Cache(Duration ttl) { this.ttlMillis = ttl.toMillis(); }

        List<PontoHistorico> getOuCarrega(String key, Supplier<List<PontoHistorico>> loader) {
            long agora = System.currentTimeMillis();
            Entry atual = map.get(key);
            if (atual != null && atual.expiresAt() > agora) return atual.value();
            List<PontoHistorico> fresh = loader.get();
            map.put(key, new Entry(fresh, agora + ttlMillis));
            return fresh;
        }
    }
}
