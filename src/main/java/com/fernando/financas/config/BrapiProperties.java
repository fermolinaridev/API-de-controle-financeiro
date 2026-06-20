package com.fernando.financas.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuração do cliente da API de cotações brapi.dev.
 * O token é grátis (cadastro em brapi.dev). Sem token, só uns poucos tickers de
 * teste de ações funcionam e as cotações de dólar/cripto ficam indisponíveis.
 */
@ConfigurationProperties(prefix = "app.brapi")
public record BrapiProperties(String baseUrl, String token) {

    public boolean temToken() {
        return token != null && !token.isBlank();
    }
}
