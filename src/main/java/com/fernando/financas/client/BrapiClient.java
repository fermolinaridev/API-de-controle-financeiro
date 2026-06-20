package com.fernando.financas.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fernando.financas.config.BrapiProperties;
import com.fernando.financas.dto.AtivoBuscaResponse;
import com.fernando.financas.dto.CotacaoResponse;
import com.fernando.financas.exception.RecursoNaoEncontradoException;
import com.fernando.financas.exception.ServicoIndisponivelException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Busca e preço atual de ativos da B3 via brapi.dev — usando o endpoint
 * {@code /quote/list}, que funciona <b>sem token</b> (token é opcional).
 *
 * Não cobre histórico (gráfico): isso fica no {@link YahooFinanceClient}.
 */
@Component
@Slf4j
public class BrapiClient {

    private static final int LIMITE_BUSCA = 10;

    private final RestClient http;
    private final BrapiProperties props;
    private final Cache cache = new Cache(Duration.ofSeconds(60));

    public BrapiClient(RestClient.Builder builder, BrapiProperties props) {
        this.props = props;
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) Duration.ofSeconds(5).toMillis());
        factory.setReadTimeout((int) Duration.ofSeconds(10).toMillis());
        this.http = builder
                .baseUrl(props.baseUrl())
                .requestFactory(factory)
                .build();
    }

    /** Busca ativos por termo (autocomplete). */
    public List<AtivoBuscaResponse> buscar(String termo) {
        JsonNode stocks = lista(termo);
        List<AtivoBuscaResponse> resultados = new ArrayList<>();
        for (JsonNode s : stocks) {
            resultados.add(new AtivoBuscaResponse(
                    s.path("stock").asText(),
                    textoOuNulo(s, "name"),
                    textoOuNulo(s, "logo"),
                    textoOuNulo(s, "type")));
            if (resultados.size() >= LIMITE_BUSCA) break;
        }
        return resultados;
    }

    /** Preço atual de uma ação/FII/BDR da B3 (match exato no {@code /quote/list}). */
    public CotacaoResponse cotacaoAcao(String ticker) {
        String alvo = ticker.trim().toUpperCase();
        JsonNode stocks = lista(alvo);
        for (JsonNode s : stocks) {
            if (alvo.equalsIgnoreCase(s.path("stock").asText())) {
                JsonNode preco = s.get("close");
                if (preco == null || preco.isNull()) break;
                return new CotacaoResponse(
                        alvo,
                        textoOuPadrao(s, "name", alvo),
                        preco.decimalValue(),
                        s.has("change") && !s.get("change").isNull() ? s.get("change").decimalValue() : null,
                        "BRL");
            }
        }
        throw new RecursoNaoEncontradoException("Ativo não encontrado: " + alvo);
    }

    private JsonNode lista(String termo) {
        return cache.get("list:" + termo.toLowerCase(), () -> {
            JsonNode body = get("/quote/list?search={termo}", Map.of("termo", termo));
            JsonNode stocks = body.get("stocks");
            return (stocks != null && stocks.isArray()) ? stocks : JsonNodeFactory.instance.arrayNode();
        });
    }

    private JsonNode get(String uriTemplate, Map<String, ?> vars) {
        try {
            JsonNode body = http.get()
                    .uri(uriTemplate, vars)
                    .headers(this::autenticar)
                    .retrieve()
                    .body(JsonNode.class);
            if (body == null || body.path("error").asBoolean(false)) {
                throw new ServicoIndisponivelException("Serviço de busca de ativos indisponível no momento");
            }
            return body;
        } catch (ServicoIndisponivelException e) {
            throw e;
        } catch (RestClientException e) {
            log.warn("Falha ao consultar brapi: {}", e.getMessage());
            throw new ServicoIndisponivelException("Serviço de busca de ativos indisponível no momento");
        }
    }

    private void autenticar(HttpHeaders headers) {
        if (props.temToken()) {
            headers.setBearerAuth(props.token());
        }
    }

    private static String textoOuNulo(JsonNode node, String campo) {
        JsonNode v = node.get(campo);
        return (v == null || v.isNull() || v.asText().isBlank()) ? null : v.asText();
    }

    private static String textoOuPadrao(JsonNode node, String campo, String padrao) {
        String v = textoOuNulo(node, campo);
        return v != null ? v : padrao;
    }

    /** Cache simples com expiração por TTL. */
    private static final class Cache {
        private record Entry(JsonNode value, long expiresAt) {}

        private final ConcurrentHashMap<String, Entry> map = new ConcurrentHashMap<>();
        private final long ttlMillis;

        Cache(Duration ttl) {
            this.ttlMillis = ttl.toMillis();
        }

        JsonNode get(String key, Supplier<JsonNode> loader) {
            long agora = System.currentTimeMillis();
            Entry atual = map.get(key);
            if (atual != null && atual.expiresAt() > agora) {
                return atual.value();
            }
            JsonNode fresh = loader.get();
            map.put(key, new Entry(fresh, agora + ttlMillis));
            return fresh;
        }
    }
}
