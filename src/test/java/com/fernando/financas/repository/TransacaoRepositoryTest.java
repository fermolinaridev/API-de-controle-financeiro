package com.fernando.financas.repository;

import com.fernando.financas.entity.Categoria;
import com.fernando.financas.entity.TipoTransacao;
import com.fernando.financas.entity.Transacao;
import com.fernando.financas.entity.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class TransacaoRepositoryTest {

    @Autowired TransacaoRepository transacaoRepository;
    @Autowired UsuarioRepository usuarioRepository;
    @Autowired CategoriaRepository categoriaRepository;

    Usuario userA;
    Usuario userB;
    Categoria catReceita;
    Categoria catDespesa;

    @BeforeEach
    void seed() {
        userA = usuarioRepository.save(Usuario.builder().nome("A").email("a@a.com").senha("x").build());
        userB = usuarioRepository.save(Usuario.builder().nome("B").email("b@b.com").senha("x").build());
        catReceita = categoriaRepository.save(Categoria.builder().nome("Salário").tipo(TipoTransacao.RECEITA).build());
        catDespesa = categoriaRepository.save(Categoria.builder().nome("Lazer").tipo(TipoTransacao.DESPESA).build());

        salvar(userA, catReceita, TipoTransacao.RECEITA, "5000", LocalDate.of(2026, 6, 1));
        salvar(userA, catDespesa, TipoTransacao.DESPESA, "300", LocalDate.of(2026, 6, 15));
        salvar(userA, catDespesa, TipoTransacao.DESPESA, "200", LocalDate.of(2026, 5, 10));
        salvar(userB, catReceita, TipoTransacao.RECEITA, "9999", LocalDate.of(2026, 6, 5));
    }

    private void salvar(Usuario u, Categoria c, TipoTransacao tipo, String valor, LocalDate data) {
        transacaoRepository.save(Transacao.builder()
                .usuario(u).categoria(c).tipo(tipo)
                .descricao("t").valor(new BigDecimal(valor)).data(data).build());
    }

    @Test
    void buscarSemFiltrosRetornaApenasDoUsuario() {
        Page<Transacao> page = transacaoRepository.buscar(userA.getId(), null, null, null, null, null, PageRequest.of(0, 10));
        assertThat(page.getContent()).hasSize(3);
        assertThat(page.getContent()).allMatch(t -> t.getUsuario().getId().equals(userA.getId()));
    }

    @Test
    void buscarFiltraPorPeriodo() {
        Page<Transacao> page = transacaoRepository.buscar(
                userA.getId(), LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 30), null, null, null, PageRequest.of(0, 10));
        assertThat(page.getContent()).hasSize(2);
    }

    @Test
    void buscarFiltraPorCategoria() {
        Page<Transacao> page = transacaoRepository.buscar(
                userA.getId(), null, null, catDespesa.getId(), null, null, PageRequest.of(0, 10));
        assertThat(page.getContent()).hasSize(2);
    }

    @Test
    void buscarFiltraPorTipo() {
        Page<Transacao> page = transacaoRepository.buscar(
                userA.getId(), null, null, null, TipoTransacao.DESPESA, null, PageRequest.of(0, 10));
        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getContent()).allMatch(t -> t.getTipo() == TipoTransacao.DESPESA);
    }

    @Test
    void buscarFiltraPorTextoCaseInsensitive() {
        transacaoRepository.save(Transacao.builder()
                .usuario(userA).categoria(catDespesa).tipo(TipoTransacao.DESPESA)
                .descricao("Mercado da esquina").valor(new BigDecimal("100"))
                .data(LocalDate.of(2026, 6, 20)).build());

        Page<Transacao> page = transacaoRepository.buscar(
                userA.getId(), null, null, null, null, "MERCADO", PageRequest.of(0, 10));
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getDescricao()).isEqualTo("Mercado da esquina");
    }

    @Test
    void somarPorTipoIsolaUsuario() {
        BigDecimal receitasA = transacaoRepository.somarPorTipoNoPeriodo(
                userA.getId(), TipoTransacao.RECEITA, LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 30));
        BigDecimal receitasB = transacaoRepository.somarPorTipoNoPeriodo(
                userB.getId(), TipoTransacao.RECEITA, LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 30));
        assertThat(receitasA).isEqualByComparingTo("5000");
        assertThat(receitasB).isEqualByComparingTo("9999");
    }
}
