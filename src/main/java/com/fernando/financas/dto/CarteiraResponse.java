package com.fernando.financas.dto;

import java.util.List;

/** Carteira completa: resumo consolidado + posições já com cotação ao vivo. */
public record CarteiraResponse(
        CarteiraResumo resumo,
        List<InvestimentoResponse> itens
) {}
