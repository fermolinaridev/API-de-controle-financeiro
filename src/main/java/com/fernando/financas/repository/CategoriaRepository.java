package com.fernando.financas.repository;

import com.fernando.financas.entity.Categoria;
import com.fernando.financas.entity.TipoTransacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    boolean existsByNomeIgnoreCaseAndTipo(String nome, TipoTransacao tipo);
    Optional<Categoria> findByNomeIgnoreCaseAndTipo(String nome, TipoTransacao tipo);
}
