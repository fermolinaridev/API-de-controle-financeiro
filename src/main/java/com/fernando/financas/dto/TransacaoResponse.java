package com.fernando.financas.dto;

import com.fernando.financas.entity.TipoTransacao;
import com.fernando.financas.entity.Transacao;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransacaoResponse(
        Long id,
        String descricao,
        BigDecimal valor,
        LocalDate data,
        TipoTransacao tipo,
        Long categoriaId,
        String categoriaNome
) {
    public static TransacaoResponse from(Transacao t) {
        return new TransacaoResponse(
                t.getId(),
                t.getDescricao(),
                t.getValor(),
                t.getData(),
                t.getTipo(),
                t.getCategoria().getId(),
                t.getCategoria().getNome()
        );
    }
}
