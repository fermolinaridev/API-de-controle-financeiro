package com.fernando.financas.repository;

import com.fernando.financas.entity.Transacao;
import com.fernando.financas.entity.TipoTransacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface TransacaoRepository extends JpaRepository<Transacao, Long> {

    @Query("""
        SELECT t FROM Transacao t
        WHERE (:inicio IS NULL OR t.data >= :inicio)
          AND (:fim    IS NULL OR t.data <= :fim)
          AND (:categoriaId IS NULL OR t.categoria.id = :categoriaId)
        """)
    Page<Transacao> buscar(
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim,
            @Param("categoriaId") Long categoriaId,
            Pageable pageable);

    @Query("""
        SELECT COALESCE(SUM(t.valor), 0) FROM Transacao t
        WHERE t.tipo = :tipo
          AND t.data BETWEEN :inicio AND :fim
        """)
    BigDecimal somarPorTipoNoPeriodo(
            @Param("tipo") TipoTransacao tipo,
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim);
}
