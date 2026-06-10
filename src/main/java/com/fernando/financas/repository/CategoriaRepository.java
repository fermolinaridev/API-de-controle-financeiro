package com.fernando.financas.repository;

import com.fernando.financas.entity.Categoria;
import com.fernando.financas.entity.TipoTransacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    /** Categorias do sistema (usuario IS NULL) + categorias do próprio usuário. */
    @Query("SELECT c FROM Categoria c WHERE c.usuario IS NULL OR c.usuario.id = :usuarioId ORDER BY c.nome")
    List<Categoria> findVisiveis(@Param("usuarioId") Long usuarioId);

    @Query("""
        SELECT c FROM Categoria c
        WHERE LOWER(c.nome) = LOWER(:nome) AND c.tipo = :tipo
          AND (c.usuario IS NULL OR c.usuario.id = :usuarioId)
        """)
    Optional<Categoria> findVisivelPorNomeETipo(
            @Param("usuarioId") Long usuarioId,
            @Param("nome") String nome,
            @Param("tipo") TipoTransacao tipo);
}
