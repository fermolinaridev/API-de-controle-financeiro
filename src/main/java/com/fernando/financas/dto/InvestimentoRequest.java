package com.fernando.financas.dto;

import com.fernando.financas.entity.TipoAtivo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record InvestimentoRequest(
        @NotBlank @Size(max = 20) String ticker,
        @NotNull TipoAtivo classe,
        @NotNull @Positive BigDecimal quantidade,
        @NotNull @Positive BigDecimal precoMedio
) {}
