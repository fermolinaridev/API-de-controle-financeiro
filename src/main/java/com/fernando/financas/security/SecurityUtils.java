package com.fernando.financas.security;

import com.fernando.financas.entity.Usuario;
import com.fernando.financas.exception.RecursoNaoEncontradoException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {}

    public static Usuario usuarioAtual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UsuarioPrincipal up) {
            return up.usuario();
        }
        throw new RecursoNaoEncontradoException("Usuário autenticado não encontrado");
    }
}
