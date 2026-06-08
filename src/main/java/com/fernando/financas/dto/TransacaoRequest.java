package com.fernando.financas.dto;

import com.fernando.financas.entity.TipoTransacao;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransacaoRequest(
        @NotBlank @Size(max = 160) String descricao,
        @NotNull @Positive BigDecimal valor,
        @NotNull LocalDate data,
        @NotNull TipoTransacao tipo,
        @NotNull Long categoriaId
) {}
