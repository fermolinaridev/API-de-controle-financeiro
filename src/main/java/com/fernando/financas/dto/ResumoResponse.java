package com.fernando.financas.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ResumoResponse(
        LocalDate inicio,
        LocalDate fim,
        BigDecimal totalReceitas,
        BigDecimal totalDespesas,
        BigDecimal saldo,
        boolean saldoNegativo
) {}
