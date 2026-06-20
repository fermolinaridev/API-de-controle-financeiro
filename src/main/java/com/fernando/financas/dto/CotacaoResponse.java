package com.fernando.financas.dto;

import java.math.BigDecimal;

/** Cotação ao vivo de um ativo, moeda ou cripto. */
public record CotacaoResponse(
        String ticker,
        String nome,
        BigDecimal preco,
        BigDecimal variacaoPercentual,
        String moeda
) {}
