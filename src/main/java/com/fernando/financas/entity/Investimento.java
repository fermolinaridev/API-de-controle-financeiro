package com.fernando.financas.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "investimento")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Investimento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Código do ativo, sempre em maiúsculas (ex.: PETR4, HGLG11, BTC). */
    @Column(nullable = false, length = 20)
    private String ticker;

    /** Nome de exibição em cache (preenchido a partir da cotação). */
    @Column(length = 120)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipoAtivo classe;

    @Column(nullable = false, precision = 18, scale = 8)
    private BigDecimal quantidade;

    /** Preço médio de compra na moeda do ativo. */
    @Column(name = "preco_medio", nullable = false, precision = 18, scale = 8)
    private BigDecimal precoMedio;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @PrePersist
    void prePersist() {
        if (criadoEm == null) criadoEm = LocalDateTime.now();
    }
}
