package com.fernando.financas.service;

import com.fernando.financas.dto.ResumoResponse;
import com.fernando.financas.dto.TransacaoRequest;
import com.fernando.financas.dto.TransacaoResponse;
import com.fernando.financas.entity.Categoria;
import com.fernando.financas.entity.TipoTransacao;
import com.fernando.financas.entity.Transacao;
import com.fernando.financas.entity.Usuario;
import com.fernando.financas.exception.RecursoNaoEncontradoException;
import com.fernando.financas.exception.RegraNegocioException;
import com.fernando.financas.repository.TransacaoRepository;
import com.fernando.financas.security.UsuarioPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

@Service
@RequiredArgsConstructor
public class TransacaoService {

    private final TransacaoRepository repository;
    private final CategoriaService categoriaService;

    @Transactional
    public TransacaoResponse criar(TransacaoRequest req) {
        Categoria categoria = categoriaService.buscarOuFalhar(req.categoriaId());
        validarCoerenciaTipo(req.tipo(), categoria);

        Usuario usuario = usuarioAtual();

        Transacao t = Transacao.builder()
                .descricao(req.descricao())
                .valor(req.valor())
                .data(req.data())
                .tipo(req.tipo())
                .categoria(categoria)
                .usuario(usuario)
                .build();
        t = repository.save(t);

        String aviso = avisoSaldoNegativo(usuario.getId(), req);
        return TransacaoResponse.from(t, aviso);
    }

    /** Retorna mensagem se a despesa fizer o saldo do mês ficar negativo, senão null. */
    private String avisoSaldoNegativo(Long usuarioId, TransacaoRequest req) {
        if (req.tipo() != TipoTransacao.DESPESA) return null;
        YearMonth ym = YearMonth.from(req.data());
        LocalDate inicio = ym.atDay(1);
        LocalDate fim = ym.atEndOfMonth();
        BigDecimal receitas = repository.somarPorTipoNoPeriodo(usuarioId, TipoTransacao.RECEITA, inicio, fim);
        BigDecimal despesas = repository.somarPorTipoNoPeriodo(usuarioId, TipoTransacao.DESPESA, inicio, fim);
        BigDecimal saldo = receitas.subtract(despesas);
        return saldo.compareTo(BigDecimal.ZERO) < 0
                ? "Atenção: esta despesa deixou seu saldo do mês negativo (" + saldo + ")"
                : null;
    }

    @Transactional(readOnly = true)
    public Page<TransacaoResponse> listar(Integer mes, Integer ano, Long categoriaId, Pageable pageable) {
        LocalDate inicio = null, fim = null;
        if (mes != null && ano != null) {
            YearMonth ym = YearMonth.of(ano, mes);
            inicio = ym.atDay(1);
            fim = ym.atEndOfMonth();
        } else if (ano != null) {
            inicio = LocalDate.of(ano, 1, 1);
            fim = LocalDate.of(ano, 12, 31);
        }
        return repository.buscar(usuarioAtual().getId(), inicio, fim, categoriaId, pageable).map(TransacaoResponse::from);
    }

    @Transactional
    public TransacaoResponse atualizar(Long id, TransacaoRequest req) {
        Long usuarioId = usuarioAtual().getId();
        Transacao t = repository.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Transação não encontrada"));
        Categoria categoria = categoriaService.buscarOuFalhar(req.categoriaId());
        validarCoerenciaTipo(req.tipo(), categoria);

        t.setDescricao(req.descricao());
        t.setValor(req.valor());
        t.setData(req.data());
        t.setTipo(req.tipo());
        t.setCategoria(categoria);
        return TransacaoResponse.from(t);
    }

    @Transactional
    public void deletar(Long id) {
        Long usuarioId = usuarioAtual().getId();
        Transacao t = repository.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Transação não encontrada"));
        repository.delete(t);
    }

    @Transactional(readOnly = true)
    public ResumoResponse resumoMesAtual() {
        Long usuarioId = usuarioAtual().getId();
        YearMonth ym = YearMonth.now();
        LocalDate inicio = ym.atDay(1);
        LocalDate fim = ym.atEndOfMonth();

        BigDecimal receitas = repository.somarPorTipoNoPeriodo(usuarioId, TipoTransacao.RECEITA, inicio, fim);
        BigDecimal despesas = repository.somarPorTipoNoPeriodo(usuarioId, TipoTransacao.DESPESA, inicio, fim);
        BigDecimal saldo = receitas.subtract(despesas);

        return new ResumoResponse(inicio, fim, receitas, despesas, saldo,
                saldo.compareTo(BigDecimal.ZERO) < 0);
    }

    private Usuario usuarioAtual() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UsuarioPrincipal up) return up.usuario();
        throw new RecursoNaoEncontradoException("Usuário autenticado não encontrado");
    }

    private void validarCoerenciaTipo(TipoTransacao tipoTransacao, Categoria categoria) {
        if (categoria.getTipo() != tipoTransacao) {
            throw new RegraNegocioException(
                    "Tipo da transação não corresponde ao tipo da categoria");
        }
    }
}
