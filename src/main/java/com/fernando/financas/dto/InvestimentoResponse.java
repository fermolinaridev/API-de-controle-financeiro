package com.fernando.financas.dto;

import com.fernando.financas.entity.TipoAtivo;

import java.math.BigDecimal;

/**
 * Posição na carteira já enriquecida com a cotação ao vivo.
 * Quando a cotação não pôde ser obtida, {@code cotacaoIndisponivel} é true e os
 * campos derivados de preço atual ficam nulos (mas o valor investido permanece).
 */
public record InvestimentoResponse(
        Long id,
        String ticker,
        String nome,
        TipoAtivo classe,
        BigDecimal quantidade,
        BigDecimal precoMedio,
        BigDecimal valorInvestido,
        BigDecimal precoAtual,
        BigDecimal valorAtual,
        BigDecimal rendimento,
        BigDecimal rendimentoPercentual,
        String moeda,
        boolean cotacaoIndisponivel
) {}
