package com.fernando.financas.dto;

import com.fernando.financas.entity.TipoTransacao;

import java.time.LocalDate;

public record TransacaoFiltro(
        Integer mes,
        Integer ano,
        LocalDate dataInicio,
        LocalDate dataFim,
        Long categoriaId,
        TipoTransacao tipo,
        String q
) {}
