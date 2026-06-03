package com.fernando.financas.service;

import com.fernando.financas.dto.AuthResponse;
import com.fernando.financas.dto.LoginRequest;
import com.fernando.financas.dto.RegisterRequest;
import com.fernando.financas.entity.Usuario;
import com.fernando.financas.exception.RegraNegocioException;
import com.fernando.financas.repository.UsuarioRepository;
import com.fernando.financas.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository repository;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authManager;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (repository.existsByEmail(req.email())) {
            throw new RegraNegocioException("E-mail já cadastrado");
        }
        Usuario u = Usuario.builder()
                .nome(req.nome())
                .email(req.email())
                .senha(encoder.encode(req.senha()))
                .build();
        u = repository.save(u);
        return montarResposta(u);
    }

    public AuthResponse login(LoginRequest req) {
        try {
            authManager.authenticate(new UsernamePasswordAuthenticationToken(req.email(), req.senha()));
        } catch (BadCredentialsException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas");
        }
        Usuario u = repository.findByEmail(req.email()).orElseThrow();
        return montarResposta(u);
    }

    private AuthResponse montarResposta(Usuario u) {
        String token = jwtService.gerarToken(u.getId(), u.getEmail());
        return new AuthResponse(token, jwtService.getExpirationSeconds(), u.getNome(), u.getEmail());
    }
}
