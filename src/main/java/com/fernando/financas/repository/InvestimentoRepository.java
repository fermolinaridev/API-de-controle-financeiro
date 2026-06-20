package com.fernando.financas.repository;

import com.fernando.financas.entity.Investimento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InvestimentoRepository extends JpaRepository<Investimento, Long> {

    List<Investimento> findByUsuarioIdOrderByTickerAsc(Long usuarioId);

    Optional<Investimento> findByIdAndUsuarioId(Long id, Long usuarioId);
}
