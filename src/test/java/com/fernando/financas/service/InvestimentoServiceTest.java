package com.fernando.financas.service;

import com.fernando.financas.client.AwesomeApiClient;
import com.fernando.financas.client.BrapiClient;
import com.fernando.financas.dto.CarteiraResponse;
import com.fernando.financas.dto.CotacaoResponse;
import com.fernando.financas.entity.Investimento;
import com.fernando.financas.entity.TipoAtivo;
import com.fernando.financas.entity.Usuario;
import com.fernando.financas.exception.ServicoIndisponivelException;
import com.fernando.financas.repository.InvestimentoRepository;
import com.fernando.financas.security.UsuarioPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvestimentoServiceTest {

    @Mock InvestimentoRepository repository;
    @Mock BrapiClient brapi;
    @Mock AwesomeApiClient awesome;
    @InjectMocks InvestimentoService service;

    @BeforeEach
    void setUp() {
        Usuario u = Usuario.builder().id(1L).nome("Test").email("t@t.com").senha("x").build();
        var auth = new UsernamePasswordAuthenticationToken(new UsuarioPrincipal(u), null);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() { SecurityContextHolder.clearContext(); }

    private Investimento acao(String ticker, String qtd, String precoMedio) {
        return Investimento.builder().id(1L).ticker(ticker).classe(TipoAtivo.ACAO)
                .quantidade(new BigDecimal(qtd)).precoMedio(new BigDecimal(precoMedio)).build();
    }

    private CotacaoResponse cotacao(String ticker, String nome, String preco) {
        return new CotacaoResponse(ticker, nome, new BigDecimal(preco), null, "BRL");
    }

    @Test
    void calculaRendimentoComPrecoAoVivo() {
        when(repository.findByUsuarioIdOrderByTickerAsc(1L)).thenReturn(List.of(acao("PETR4", "10", "30")));
        when(brapi.cotacaoAcao("PETR4")).thenReturn(cotacao("PETR4", "Empresa PETR4", "40"));

        CarteiraResponse carteira = service.listarCarteira();

        var item = carteira.itens().get(0);
        assertThat(item.cotacaoIndisponivel()).isFalse();
        assertThat(item.valorInvestido()).isEqualByComparingTo("300.00");
        assertThat(item.valorAtual()).isEqualByComparingTo("400.00");
        assertThat(item.rendimento()).isEqualByComparingTo("100.00");
        assertThat(item.rendimentoPercentual()).isEqualByComparingTo("33.33");
        assertThat(item.nome()).isEqualTo("Empresa PETR4");

        assertThat(carteira.resumo().totalInvestido()).isEqualByComparingTo("300.00");
        assertThat(carteira.resumo().valorAtualTotal()).isEqualByComparingTo("400.00");
        assertThat(carteira.resumo().rendimentoTotal()).isEqualByComparingTo("100.00");
    }

    @Test
    void calculaRendimentoDeCriptoFracionada() {
        Investimento btc = Investimento.builder().id(2L).ticker("BTC").classe(TipoAtivo.CRIPTO)
                .quantidade(new BigDecimal("0.5")).precoMedio(new BigDecimal("200000")).build();
        when(repository.findByUsuarioIdOrderByTickerAsc(1L)).thenReturn(List.of(btc));
        when(awesome.cotacao("BTC-BRL", "BTC", "BTC")).thenReturn(cotacao("BTC", "Bitcoin", "300000"));

        var item = service.listarCarteira().itens().get(0);

        assertThat(item.valorInvestido()).isEqualByComparingTo("100000.00");
        assertThat(item.valorAtual()).isEqualByComparingTo("150000.00");
        assertThat(item.rendimento()).isEqualByComparingTo("50000.00");
        assertThat(item.nome()).isEqualTo("Bitcoin");
    }

    @Test
    void degradaQuandoCotacaoIndisponivel() {
        when(repository.findByUsuarioIdOrderByTickerAsc(1L)).thenReturn(List.of(acao("VALE3", "5", "60")));
        when(brapi.cotacaoAcao("VALE3")).thenThrow(new ServicoIndisponivelException("fora do ar"));

        CarteiraResponse carteira = service.listarCarteira();
        var item = carteira.itens().get(0);

        assertThat(item.cotacaoIndisponivel()).isTrue();
        assertThat(item.precoAtual()).isNull();
        assertThat(item.valorAtual()).isNull();
        assertThat(item.valorInvestido()).isEqualByComparingTo("300.00");
        assertThat(carteira.resumo().valorAtualTotal()).isEqualByComparingTo("300.00");
        assertThat(carteira.resumo().rendimentoTotal()).isEqualByComparingTo("0.00");
    }
}
