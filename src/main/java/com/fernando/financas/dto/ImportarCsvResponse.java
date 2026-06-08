package com.fernando.financas.dto;

import java.util.List;

public record ImportarCsvResponse(int importadas, int falhas, List<String> erros) {}
