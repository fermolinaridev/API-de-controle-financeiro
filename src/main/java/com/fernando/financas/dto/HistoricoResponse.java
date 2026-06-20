package com.fernando.financas.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Série histórica de preço de um ativo, para renderizar o gráfico. */
public record HistoricoResponse(
        String ticker,
        String nome,
        List<PontoHistorico> pontos
) {
    public record PontoHistorico(LocalDate data, BigDecimal fechamento) {}
}
