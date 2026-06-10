package com.fernando.financas.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "categoria")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipoTransacao tipo;

    /** null = categoria do sistema (visível a todos, imutável) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    public boolean isDoSistema() {
        return usuario == null;
    }
}
