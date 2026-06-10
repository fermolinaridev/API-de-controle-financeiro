package com.fernando.financas.service;

import com.fernando.financas.dto.CategoriaRequest;
import com.fernando.financas.dto.CategoriaResponse;
import com.fernando.financas.entity.Categoria;
import com.fernando.financas.entity.Usuario;
import com.fernando.financas.exception.RecursoNaoEncontradoException;
import com.fernando.financas.exception.RegraNegocioException;
import com.fernando.financas.repository.CategoriaRepository;
import com.fernando.financas.repository.TransacaoRepository;
import com.fernando.financas.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository repository;
    private final TransacaoRepository transacaoRepository;

    @Transactional(readOnly = true)
    public List<CategoriaResponse> listar() {
        Long usuarioId = SecurityUtils.usuarioAtual().getId();
        return repository.findVisiveis(usuarioId).stream().map(CategoriaResponse::from).toList();
    }

    @Transactional
    public CategoriaResponse criar(CategoriaRequest req) {
        Usuario usuario = SecurityUtils.usuarioAtual();
        if (repository.findVisivelPorNomeETipo(usuario.getId(), req.nome(), req.tipo()).isPresent()) {
            throw new RegraNegocioException("Já existe uma categoria com esse nome e tipo");
        }
        Categoria salva = repository.save(
                Categoria.builder().nome(req.nome()).tipo(req.tipo()).usuario(usuario).build());
        return CategoriaResponse.from(salva);
    }

    @Transactional
    public CategoriaResponse atualizar(Long id, CategoriaRequest req) {
        Categoria c = buscarPropriaOuFalhar(id);
        boolean mudouNomeOuTipo = !c.getNome().equalsIgnoreCase(req.nome()) || c.getTipo() != req.tipo();
        if (mudouNomeOuTipo) {
            repository.findVisivelPorNomeETipo(SecurityUtils.usuarioAtual().getId(), req.nome(), req.tipo())
                    .filter(existente -> !existente.getId().equals(id))
                    .ifPresent(existente -> {
                        throw new RegraNegocioException("Já existe uma categoria com esse nome e tipo");
                    });
        }
        if (c.getTipo() != req.tipo() && transacaoRepository.existsByCategoriaId(id)) {
            throw new RegraNegocioException("Não é possível mudar o tipo: existem transações usando essa categoria");
        }
        c.setNome(req.nome());
        c.setTipo(req.tipo());
        return CategoriaResponse.from(c);
    }

    @Transactional
    public void deletar(Long id) {
        Categoria c = buscarPropriaOuFalhar(id);
        if (transacaoRepository.existsByCategoriaId(id)) {
            throw new RegraNegocioException("Não é possível excluir: existem transações usando essa categoria");
        }
        repository.delete(c);
    }

    /** Categoria visível ao usuário atual (do sistema ou própria) — para uso em transações. */
    @Transactional(readOnly = true)
    public Categoria buscarOuFalhar(Long id) {
        Long usuarioId = SecurityUtils.usuarioAtual().getId();
        Categoria c = repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria não encontrada"));
        if (!c.isDoSistema() && !c.getUsuario().getId().equals(usuarioId)) {
            // categoria de outro usuário: tratar como inexistente (não vaza existência)
            throw new RecursoNaoEncontradoException("Categoria não encontrada");
        }
        return c;
    }

    /** Categoria própria do usuário — para edição/exclusão. Sistema é imutável. */
    private Categoria buscarPropriaOuFalhar(Long id) {
        Categoria c = buscarOuFalhar(id);
        if (c.isDoSistema()) {
            throw new RegraNegocioException("Categorias do sistema não podem ser alteradas ou excluídas");
        }
        return c;
    }
}
