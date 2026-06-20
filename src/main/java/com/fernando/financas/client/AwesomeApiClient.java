package com.fernando.financas.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fernando.financas.dto.CotacaoResponse;
import com.fernando.financas.exception.ServicoIndisponivelException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Cotações de moedas e criptos via AwesomeAPI (economia.awesomeapi.com.br) —
 * pública e <b>sem chave</b>. Usada para dólar, Bitcoin e criptos da carteira.
 */
@Component
@Slf4j
public class AwesomeApiClient {

    private final RestClient http;
    private final Cache cache = new Cache(Duration.ofSeconds(60));

    public AwesomeApiClient(RestClient.Builder builder) {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) Duration.ofSeconds(5).toMillis());
        factory.setReadTimeout((int) Duration.ofSeconds(10).toMillis());
        this.http = builder
                .baseUrl("https://economia.awesomeapi.com.br")
                .requestFactory(factory)
                .build();
    }

    /**
     * Cotação de um par, ex.: {@code USD-BRL} ou {@code BTC-BRL}.
     * @param ticker rótulo a devolver no DTO (ex.: "USD-BRL" ou "BTC")
     * @param nomePadrao nome amigável caso a API não traga um
     */
    public CotacaoResponse cotacao(String par, String ticker, String nomePadrao) {
        String chave = par.replace("-", "").toUpperCase(); // USD-BRL -> USDBRL
        JsonNode node = cache.get("awesome:" + par, () -> {
            JsonNode body = get("/json/last/{par}", Map.of("par", par));
            JsonNode item = body.get(chave);
            if (item == null || item.isNull()) {
                throw new ServicoIndisponivelException("Cotação indisponível no momento");
            }
            return item;
        });

        BigDecimal preco = decimal(node, "bid");
        if (preco == null) {
            throw new ServicoIndisponivelException("Cotação indisponível no momento");
        }
        String nome = node.path("name").asText(nomePadrao);
        return new CotacaoResponse(ticker, nome, preco, decimal(node, "pctChange"), "BRL");
    }

    private JsonNode get(String uriTemplate, Map<String, ?> vars) {
        try {
            JsonNode body = http.get().uri(uriTemplate, vars).retrieve().body(JsonNode.class);
            if (body == null || body.path("status").asInt(200) >= 400) {
                throw new ServicoIndisponivelException("Cotação indisponível no momento");
            }
            return body;
        } catch (ServicoIndisponivelException e) {
            throw e;
        } catch (RestClientException e) {
            log.warn("Falha ao consultar AwesomeAPI: {}", e.getMessage());
            throw new ServicoIndisponivelException("Cotação indisponível no momento");
        }
    }

    /** AwesomeAPI devolve números como string ("5.1473"). */
    private static BigDecimal decimal(JsonNode node, String campo) {
        JsonNode v = node.get(campo);
        if (v == null || v.isNull()) return null;
        if (v.isNumber()) return v.decimalValue();
        String txt = v.asText().trim();
        if (txt.isBlank()) return null;
        try {
            return new BigDecimal(txt);
        } catch (NumberFormatException e) {
            return null;
        }
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
