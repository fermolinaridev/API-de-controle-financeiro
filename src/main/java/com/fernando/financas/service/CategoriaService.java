package com.fernando.financas.service;

import com.fernando.financas.dto.CategoriaRequest;
import com.fernando.financas.dto.CategoriaResponse;
import com.fernando.financas.entity.Categoria;
import com.fernando.financas.exception.RecursoNaoEncontradoException;
import com.fernando.financas.exception.RegraNegocioException;
import com.fernando.financas.repository.CategoriaRepository;
import com.fernando.financas.repository.TransacaoRepository;
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
        return repository.findAll().stream().map(CategoriaResponse::from).toList();
    }

    @Transactional
    public CategoriaResponse criar(CategoriaRequest req) {
        if (repository.existsByNomeIgnoreCaseAndTipo(req.nome(), req.tipo())) {
            throw new RegraNegocioException("Já existe uma categoria com esse nome e tipo");
        }
        Categoria salva = repository.save(
                Categoria.builder().nome(req.nome()).tipo(req.tipo()).build());
        return CategoriaResponse.from(salva);
    }

    @Transactional
    public CategoriaResponse atualizar(Long id, CategoriaRequest req) {
        Categoria c = buscarOuFalhar(id);
        if (!c.getNome().equalsIgnoreCase(req.nome()) || c.getTipo() != req.tipo()) {
            if (repository.existsByNomeIgnoreCaseAndTipo(req.nome(), req.tipo())) {
                throw new RegraNegocioException("Já existe uma categoria com esse nome e tipo");
            }
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
        Categoria c = buscarOuFalhar(id);
        if (transacaoRepository.existsByCategoriaId(id)) {
            throw new RegraNegocioException("Não é possível excluir: existem transações usando essa categoria");
        }
        repository.delete(c);
    }

    @Transactional(readOnly = true)
    public Categoria buscarOuFalhar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria não encontrada"));
    }
}
