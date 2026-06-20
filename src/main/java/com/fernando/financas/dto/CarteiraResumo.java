package com.fernando.financas.dto;

import java.math.BigDecimal;

public record CarteiraResumo(
        BigDecimal totalInvestido,
        BigDecimal valorAtualTotal,
        BigDecimal rendimentoTotal,
        BigDecimal rendimentoPercentualTotal
) {}
