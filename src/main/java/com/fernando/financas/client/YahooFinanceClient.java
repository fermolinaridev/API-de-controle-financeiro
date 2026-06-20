package com.fernando.financas.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fernando.financas.dto.HistoricoResponse.PontoHistorico;
import com.fernando.financas.exception.ServicoIndisponivelException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Série histórica de preço (para o gráfico) via Yahoo Finance — público e
 * <b>sem chave</b>. Cobre B3 (sufixo {@code .SA}) e ativos dos EUA.
 *
 * É uma API não-oficial: pode limitar/instabilizar. Falhas são tratadas como
 * {@link ServicoIndisponivelException} e degradam graciosamente na UI.
 */
@Component
@Slf4j
public class YahooFinanceClient {

    private static final ZoneId FUSO_B3 = ZoneId.of("America/Sao_Paulo");
    private static final String USER_AGENT =
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 "
            + "(KHTML, like Gecko) Chrome/120.0 Safari/537.36";

    private final RestClient http;
    private final Cache cache = new Cache(Duration.ofSeconds(60));

    public YahooFinanceClient(RestClient.Builder builder) {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) Duration.ofSeconds(5).toMillis());
        factory.setReadTimeout((int) Duration.ofSeconds(10).toMillis());
        this.http = builder
                .baseUrl("https://query1.finance.yahoo.com")
                .requestFactory(factory)
                .build();
    }

    /** Histórico de fechamento de um ativo. {@code ticker} no formato da B3 (ex.: PETR4). */
    public List<PontoHistorico> historico(String ticker, String range, String interval) {
        String simbolo = paraSimboloYahoo(ticker);
        JsonNode result = cache.get("yahoo:" + simbolo + ":" + range + ":" + interval, () -> {
            JsonNode body = get(simbolo, range, interval);
            JsonNode chart = body.path("chart");
            if (chart.path("error") != null && !chart.path("error").isNull()) {
                throw new ServicoIndisponivelException("Gráfico indisponível no momento");
            }
            JsonNode r = chart.path("result");
            if (!r.isArray() || r.isEmpty()) {
                throw new ServicoIndisponivelException("Gráfico indisponível no momento");
            }
            return r.get(0);
        });

        JsonNode timestamps = result.path("timestamp");
        JsonNode closes = result.path("indicators").path("quote").path(0).path("close");
        if (!timestamps.isArray() || !closes.isArray()) {
            return List.of();
        }

        List<PontoHistorico> pontos = new ArrayList<>();
        for (int i = 0; i < timestamps.size(); i++) {
            JsonNode fechamento = closes.get(i);
            if (fechamento == null || fechamento.isNull()) continue;
            LocalDate data = Instant.ofEpochSecond(timestamps.get(i).asLong()).atZone(FUSO_B3).toLocalDate();
            pontos.add(new PontoHistorico(data, fechamento.decimalValue()));
        }
        return pontos;
    }

    private JsonNode get(String simbolo, String range, String interval) {
        try {
            JsonNode body = http.get()
                    .uri("/v8/finance/chart/{s}?range={r}&interval={i}",
                            Map.of("s", simbolo, "r", range, "i", interval))
                    .header("User-Agent", USER_AGENT)
                    .retrieve()
                    .body(JsonNode.class);
            if (body == null) {
                throw new ServicoIndisponivelException("Gráfico indisponível no momento");
            }
            return body;
        } catch (ServicoIndisponivelException e) {
            throw e;
        } catch (RestClientException e) {
            log.warn("Falha ao consultar Yahoo Finance ({}): {}", simbolo, e.getMessage());
            throw new ServicoIndisponivelException(
                    "Gráfico temporariamente indisponível (fonte externa). Tente novamente em instantes.");
        }
    }

    /** PETR4 -> PETR4.SA (B3); AAPL -> AAPL (EUA); já-sufixado/cripto -> inalterado. */
    static String paraSimboloYahoo(String ticker) {
        String t = ticker.trim().toUpperCase();
        if (t.contains(".") || t.contains("-")) return t;
        char ultimo = t.charAt(t.length() - 1);
        return Character.isDigit(ultimo) ? t + ".SA" : t;
    }

    private static final class Cache {
        private record Entry(JsonNode value, long expiresAt) {}

        private final ConcurrentHashMap<String, Entry> map = new ConcurrentHashMap<>();
        private final long ttlMillis;

        Cache(Duration ttl) { this.ttlMillis = ttl.toMillis(); }

        JsonNode get(String key, Supplier<JsonNode> loader) {
            long agora = System.currentTimeMillis();
            Entry atual = map.get(key);
            if (atual != null && atual.expiresAt() > agora) return atual.value();
            JsonNode fresh = loader.get();
            map.put(key, new Entry(fresh, agora + ttlMillis));
            return fresh;
        }
    }
}
