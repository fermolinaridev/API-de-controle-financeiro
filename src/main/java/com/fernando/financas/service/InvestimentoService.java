package com.fernando.financas.service;

import com.fernando.financas.client.AwesomeApiClient;
import com.fernando.financas.client.BrapiClient;
import com.fernando.financas.dto.CarteiraResponse;
import com.fernando.financas.dto.CarteiraResumo;
import com.fernando.financas.dto.CotacaoResponse;
import com.fernando.financas.dto.InvestimentoRequest;
import com.fernando.financas.dto.InvestimentoResponse;
import com.fernando.financas.entity.Investimento;
import com.fernando.financas.entity.TipoAtivo;
import com.fernando.financas.entity.Usuario;
import com.fernando.financas.exception.RecursoNaoEncontradoException;
import com.fernando.financas.repository.InvestimentoRepository;
import com.fernando.financas.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvestimentoService {

    private final InvestimentoRepository repository;
    private final BrapiClient brapi;
    private final AwesomeApiClient awesome;

    /** Cotação resolvida de um ativo. */
    private record Cotacao(BigDecimal preco, String nome, String moeda) {}

    @Transactional(readOnly = true)
    public CarteiraResponse listarCarteira() {
        Long usuarioId = SecurityUtils.usuarioAtual().getId();
        List<Investimento> ativos = repository.findByUsuarioIdOrderByTickerAsc(usuarioId);

        Map<String, Cotacao> precos = new HashMap<>();
        ativos.forEach(a -> precos.computeIfAbsent(a.getTicker(), t -> buscarCotacao(t, a.getClasse())));

        List<InvestimentoResponse> itens = ativos.stream()
                .map(a -> enriquecer(a, precos.get(a.getTicker())))
                .toList();

        return new CarteiraResponse(resumir(itens), itens);
    }

    @Transactional
    public InvestimentoResponse criar(InvestimentoRequest req) {
        Usuario usuario = SecurityUtils.usuarioAtual();
        String ticker = normalizar(req.ticker());
        Cotacao cotacao = buscarCotacao(ticker, req.classe());

        Investimento salvo = repository.save(Investimento.builder()
                .ticker(ticker)
                .nome(cotacao != null ? cotacao.nome() : null)
                .classe(req.classe())
                .quantidade(req.quantidade())
                .precoMedio(req.precoMedio())
                .usuario(usuario)
                .build());

        return enriquecer(salvo, cotacao);
    }

    @Transactional
    public InvestimentoResponse atualizar(Long id, InvestimentoRequest req) {
        Long usuarioId = SecurityUtils.usuarioAtual().getId();
        Investimento a = repository.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Investimento não encontrado"));

        String ticker = normalizar(req.ticker());
        boolean mudouAtivo = !ticker.equals(a.getTicker()) || a.getClasse() != req.classe();
        a.setTicker(ticker);
        a.setClasse(req.classe());
        a.setQuantidade(req.quantidade());
        a.setPrecoMedio(req.precoMedio());

        Cotacao cotacao = buscarCotacao(ticker, req.classe());
        if (mudouAtivo && cotacao != null) {
            a.setNome(cotacao.nome());
        }
        return enriquecer(a, cotacao);
    }

    @Transactional
    public void deletar(Long id) {
        Long usuarioId = SecurityUtils.usuarioAtual().getId();
        Investimento a = repository.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Investimento não encontrado"));
        repository.delete(a);
    }

    // --- cotações ------------------------------------------------------------

    /** Cotação de um ativo conforme a classe. Nunca lança: devolve null se indisponível. */
    private Cotacao buscarCotacao(String ticker, TipoAtivo classe) {
        try {
            CotacaoResponse c = classe == TipoAtivo.CRIPTO
                    ? awesome.cotacao(ticker + "-BRL", ticker, ticker)
                    : brapi.cotacaoAcao(ticker);
            return new Cotacao(c.preco(), c.nome(), c.moeda());
        } catch (RuntimeException e) {
            log.warn("Cotação de {} indisponível: {}", ticker, e.getMessage());
            return null;
        }
    }

    // --- montagem dos DTOs ---------------------------------------------------

    private InvestimentoResponse enriquecer(Investimento a, Cotacao cotacao) {
        BigDecimal valorInvestido = escala(a.getQuantidade().multiply(a.getPrecoMedio()));

        if (cotacao == null || cotacao.preco() == null) {
            return new InvestimentoResponse(a.getId(), a.getTicker(), a.getNome(), a.getClasse(),
                    a.getQuantidade(), a.getPrecoMedio(), valorInvestido,
                    null, null, null, null, null, true);
        }

        BigDecimal precoAtual = escala(cotacao.preco());
        BigDecimal valorAtual = escala(a.getQuantidade().multiply(cotacao.preco()));
        BigDecimal rendimento = valorAtual.subtract(valorInvestido);
        BigDecimal rendimentoPercentual = rendimento
                .divide(valorInvestido, 6, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
        String nome = cotacao.nome() != null ? cotacao.nome() : a.getNome();

        return new InvestimentoResponse(a.getId(), a.getTicker(), nome, a.getClasse(),
                a.getQuantidade(), a.getPrecoMedio(), valorInvestido,
                precoAtual, valorAtual, rendimento, rendimentoPercentual, cotacao.moeda(), false);
    }

    private CarteiraResumo resumir(List<InvestimentoResponse> itens) {
        BigDecimal totalInvestido = BigDecimal.ZERO;
        BigDecimal valorAtualTotal = BigDecimal.ZERO;
        for (InvestimentoResponse i : itens) {
            totalInvestido = totalInvestido.add(i.valorInvestido());
            // sem cotação, a posição entra pelo valor investido (rendimento 0) para não distorcer o total
            valorAtualTotal = valorAtualTotal.add(i.valorAtual() != null ? i.valorAtual() : i.valorInvestido());
        }
        BigDecimal rendimentoTotal = valorAtualTotal.subtract(totalInvestido);
        BigDecimal percentual = totalInvestido.signum() > 0
                ? rendimentoTotal.divide(totalInvestido, 6, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        return new CarteiraResumo(escala(totalInvestido), escala(valorAtualTotal),
                escala(rendimentoTotal), percentual);
    }

    private static BigDecimal escala(BigDecimal v) {
        return v.setScale(2, RoundingMode.HALF_UP);
    }

    private static String normalizar(String ticker) {
        return ticker.trim().toUpperCase();
    }
}
