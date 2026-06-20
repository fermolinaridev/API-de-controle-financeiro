package com.fernando.financas.dto;

/** Resultado da busca de ativos (autocomplete). */
public record AtivoBuscaResponse(
        String ticker,
        String nome,
        String logo,
        String tipo
) {}
