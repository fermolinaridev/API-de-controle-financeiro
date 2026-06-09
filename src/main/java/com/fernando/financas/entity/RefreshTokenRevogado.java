package com.fernando.financas.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_token_revogado")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshTokenRevogado {

    @Id
    @Column(length = 64)
    private String jti;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "revogado_em", nullable = false, updatable = false)
    private LocalDateTime revogadoEm;

    @PrePersist
    void prePersist() {
        if (revogadoEm == null) revogadoEm = LocalDateTime.now();
    }
}
