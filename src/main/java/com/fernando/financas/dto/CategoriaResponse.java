package com.fernando.financas.dto;

import com.fernando.financas.entity.Categoria;
import com.fernando.financas.entity.TipoTransacao;

public record CategoriaResponse(Long id, String nome, TipoTransacao tipo, boolean doSistema) {
    public static CategoriaResponse from(Categoria c) {
        return new CategoriaResponse(c.getId(), c.getNome(), c.getTipo(), c.isDoSistema());
    }
}
