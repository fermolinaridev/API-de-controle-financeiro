package com.fernando.financas.controller;

import com.fernando.financas.dto.AtivoBuscaResponse;
import com.fernando.financas.dto.CotacaoResponse;
import com.fernando.financas.dto.HistoricoResponse;
import com.fernando.financas.service.MercadoService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mercado")
@RequiredArgsConstructor
@Tag(name = "Mercado")
public class MercadoController {

    private final MercadoService service;

    @GetMapping("/dolar")
    public CotacaoResponse dolar() {
        return service.dolar();
    }

    @GetMapping("/bitcoin")
    public CotacaoResponse bitcoin() {
        return service.bitcoin();
    }

    @GetMapping("/buscar")
    public List<AtivoBuscaResponse> buscar(
            @Parameter(description = "Termo de busca (ex.: PETR, vale)") @RequestParam("q") String q) {
        return service.buscar(q);
    }

    @GetMapping("/cotacao/{ticker}")
    public CotacaoResponse cotacao(@PathVariable String ticker) {
        return service.cotacao(ticker);
    }

    @GetMapping("/historico/{ticker}")
    public HistoricoResponse historico(
            @PathVariable String ticker,
            @Parameter(description = "Período: 1mo, 3mo, 6mo, 1y, 5y...") @RequestParam(defaultValue = "3mo") String range,
            @Parameter(description = "Intervalo: 1d, 1wk, 1mo...") @RequestParam(defaultValue = "1d") String interval) {
        return service.historico(ticker, range, interval);
    }
}
