package com.fernando.financas.service;

import com.fernando.financas.dto.TransacaoRequest;
import com.fernando.financas.entity.Categoria;
import com.fernando.financas.entity.TipoTransacao;
import com.fernando.financas.entity.Usuario;
import com.fernando.financas.exception.RegraNegocioException;
import com.fernando.financas.repository.TransacaoRepository;
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
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransacaoServiceTest {

    @Mock TransacaoRepository repository;
    @Mock CategoriaService categoriaService;
    @InjectMocks TransacaoService service;

    @BeforeEach
    void setUp() {
        Usuario u = Usuario.builder().id(1L).nome("Test").email("t@t.com").senha("x").build();
        var auth = new UsernamePasswordAuthenticationToken(new UsuarioPrincipal(u), null);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() { SecurityContextHolder.clearContext(); }

    @Test
    void deveCriarTransacaoQuandoTipoCondizComCategoria() {
        Categoria cat = Categoria.builder().id(10L).nome("Salário").tipo(TipoTransacao.RECEITA).build();
        when(categoriaService.buscarOuFalhar(10L)).thenReturn(cat);
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        var req = new TransacaoRequest("Salário", new BigDecimal("5000"), LocalDate.now(), TipoTransacao.RECEITA, 10L);
        var resp = service.criar(req);

        assert resp.descricao().equals("Salário");
        verify(repository).save(any());
    }

    @Test
    void deveRecusarQuandoTipoDivergeDaCategoria() {
        Categoria cat = Categoria.builder().id(10L).nome("Alimentação").tipo(TipoTransacao.DESPESA).build();
        when(categoriaService.buscarOuFalhar(10L)).thenReturn(cat);

        var req = new TransacaoRequest("Compra", new BigDecimal("100"), LocalDate.now(), TipoTransacao.RECEITA, 10L);

        assertThatThrownBy(() -> service.criar(req))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("não corresponde");

        verify(repository, never()).save(any());
    }
}
