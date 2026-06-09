package com.fernando.financas.repository;

import com.fernando.financas.entity.RefreshTokenRevogado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface RefreshTokenRevogadoRepository extends JpaRepository<RefreshTokenRevogado, String> {

    @Modifying
    @Query("DELETE FROM RefreshTokenRevogado r WHERE r.expiresAt < :now")
    int deleteExpiradosAntesDe(@Param("now") LocalDateTime now);
}
