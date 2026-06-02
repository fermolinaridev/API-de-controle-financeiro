package com.fernando.financas.service;

import com.fernando.financas.dto.CategoriaRequest;
import com.fernando.financas.dto.CategoriaResponse;
import com.fernando.financas.entity.Categoria;
import com.fernando.financas.exception.RecursoNaoEncontradoException;
import com.fernando.financas.repository.CategoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository repository;

    @Transactional(readOnly = true)
    public List<CategoriaResponse> listar() {
        return repository.findAll().stream().map(CategoriaResponse::from).toList();
    }

    @Transactional
    public CategoriaResponse criar(CategoriaRequest req) {
        Categoria salva = repository.save(
                Categoria.builder().nome(req.nome()).tipo(req.tipo()).build());
        return CategoriaResponse.from(salva);
    }

    @Transactional(readOnly = true)
    public Categoria buscarOuFalhar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria não encontrada"));
    }
}
