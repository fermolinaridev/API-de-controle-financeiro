package com.fernando.financas.service;

import com.fernando.financas.dto.AuthResponse;
import com.fernando.financas.dto.LoginRequest;
import com.fernando.financas.dto.RefreshRequest;
import com.fernando.financas.dto.RegisterRequest;
import com.fernando.financas.entity.RefreshTokenRevogado;
import com.fernando.financas.entity.Usuario;
import com.fernando.financas.exception.RegraNegocioException;
import com.fernando.financas.repository.RefreshTokenRevogadoRepository;
import com.fernando.financas.repository.UsuarioRepository;
import com.fernando.financas.security.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository repository;
    private final RefreshTokenRevogadoRepository blacklist;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authManager;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (repository.existsByEmail(req.email())) {
            throw new RegraNegocioException("E-mail já cadastrado");
        }
        Usuario u = repository.save(Usuario.builder()
                .nome(req.nome())
                .email(req.email())
                .senha(encoder.encode(req.senha()))
                .build());
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

    @Transactional
    public AuthResponse refresh(RefreshRequest req) {
        Claims claims = parseRefreshOuFalhar(req.refreshToken());
        if (claims.getId() != null && blacklist.existsById(claims.getId())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token revogado");
        }
        Usuario u = repository.findById(Long.valueOf(claims.getSubject()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não encontrado"));

        // rotação: o refresh usado é revogado; só o novo emitido continua válido
        revogar(claims);

        return montarResposta(u);
    }

    @Transactional
    public void logout(RefreshRequest req) {
        Claims claims;
        try {
            claims = parseRefreshOuFalhar(req.refreshToken());
        } catch (ResponseStatusException e) {
            // logout idempotente: token inválido/expirado também devolve OK
            return;
        }
        revogar(claims);
    }

    private void revogar(Claims claims) {
        String jti = claims.getId();
        if (jti == null || blacklist.existsById(jti)) return;

        LocalDateTime exp = claims.getExpiration().toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDateTime();
        blacklist.save(RefreshTokenRevogado.builder().jti(jti).expiresAt(exp).build());
    }

    private Claims parseRefreshOuFalhar(String token) {
        Claims claims;
        try {
            claims = jwtService.parse(token);
        } catch (JwtException | IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token inválido");
        }
        if (!JwtService.TYPE_REFRESH.equals(claims.get("type", String.class))) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token não é refresh");
        }
        return claims;
    }

    private AuthResponse montarResposta(Usuario u) {
        String access = jwtService.gerarAccessToken(u.getId(), u.getEmail());
        String refresh = jwtService.gerarRefreshToken(u.getId(), u.getEmail());
        return new AuthResponse(access, refresh, jwtService.getAccessExpirationSeconds(), u.getNome(), u.getEmail());
    }
}
