package com.fernando.financas.service;

import com.fernando.financas.client.AwesomeApiClient;
import com.fernando.financas.client.BrapiClient;
import com.fernando.financas.client.YahooFinanceClient;
import com.fernando.financas.dto.AtivoBuscaResponse;
import com.fernando.financas.dto.CotacaoResponse;
import com.fernando.financas.dto.HistoricoResponse;
import com.fernando.financas.dto.HistoricoResponse.PontoHistorico;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Dados de mercado sem necessidade de cadastro:
 * <ul>
 *   <li>dólar/BTC via AwesomeAPI;</li>
 *   <li>busca e preço atual de ações via brapi ({@code /quote/list}, sem token);</li>
 *   <li>histórico (gráfico) via Yahoo Finance.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class MercadoService {

    private final AwesomeApiClient awesome;
    private final BrapiClient brapi;
    private final YahooFinanceClient yahoo;

    public CotacaoResponse dolar() {
        return awesome.cotacao("USD-BRL", "USD-BRL", "Dólar Americano");
    }

    public CotacaoResponse bitcoin() {
        return awesome.cotacao("BTC-BRL", "BTC", "Bitcoin");
    }

    public List<AtivoBuscaResponse> buscar(String termo) {
        return brapi.buscar(termo);
    }

    public CotacaoResponse cotacao(String ticker) {
        return brapi.cotacaoAcao(ticker);
    }

    public HistoricoResponse historico(String ticker, String range, String interval) {
        String alvo = ticker.trim().toUpperCase();
        List<PontoHistorico> pontos = yahoo.historico(alvo, range, interval);
        return new HistoricoResponse(alvo, nomeDe(alvo), pontos);
    }

    /** Nome amigável via brapi (best-effort); cai no próprio ticker se indisponível. */
    private String nomeDe(String ticker) {
        try {
            return brapi.cotacaoAcao(ticker).nome();
        } catch (RuntimeException e) {
            return ticker;
        }
    }
}
