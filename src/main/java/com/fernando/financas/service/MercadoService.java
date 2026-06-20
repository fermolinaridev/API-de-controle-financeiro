package com.fernando.financas.service;

import com.fernando.financas.client.AwesomeApiClient;
import com.fernando.financas.client.BrapiClient;
import com.fernando.financas.client.MfinanceClient;
import com.fernando.financas.dto.AtivoBuscaResponse;
import com.fernando.financas.dto.CotacaoResponse;
import com.fernando.financas.dto.HistoricoResponse;
import com.fernando.financas.dto.HistoricoResponse.PontoHistorico;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Dados de mercado sem necessidade de cadastro:
 * <ul>
 *   <li>dólar/BTC via AwesomeAPI;</li>
 *   <li>busca e preço atual de ações via brapi ({@code /quote/list}, sem token);</li>
 *   <li>histórico (gráfico) via mfinance.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class MercadoService {

    /** Aproximação de pregões por período, para recortar o histórico (~3 meses disponíveis). */
    private static final Map<String, Integer> PREGOES_POR_RANGE = Map.of(
            "1mo", 22, "3mo", 66, "6mo", 132, "1y", 252);

    private final AwesomeApiClient awesome;
    private final BrapiClient brapi;
    private final MfinanceClient mfinance;

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
        List<PontoHistorico> pontos = mfinance.historico(alvo);

        // recorta os últimos N pregões conforme o período pedido
        int limite = PREGOES_POR_RANGE.getOrDefault(range, pontos.size());
        if (pontos.size() > limite) {
            pontos = pontos.subList(pontos.size() - limite, pontos.size());
        }
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
