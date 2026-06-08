package com.fernando.financas.security;

import com.fernando.financas.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    public static final String TYPE_ACCESS = "access";
    public static final String TYPE_REFRESH = "refresh";

    private final SecretKey key;
    private final long accessMs;
    private final long refreshMs;

    public JwtService(JwtProperties props) {
        this.key = Keys.hmacShaKeyFor(props.secret().getBytes(StandardCharsets.UTF_8));
        this.accessMs = props.accessTokenMinutes() * 60_000L;
        this.refreshMs = props.refreshTokenDays() * 24 * 3600_000L;
    }

    public String gerarAccessToken(Long usuarioId, String email) {
        return gerar(usuarioId, email, TYPE_ACCESS, accessMs);
    }

    public String gerarRefreshToken(Long usuarioId, String email) {
        return gerar(usuarioId, email, TYPE_REFRESH, refreshMs);
    }

    private String gerar(Long usuarioId, String email, String tipo, long durationMs) {
        Date agora = new Date();
        return Jwts.builder()
                .subject(String.valueOf(usuarioId))
                .claim("email", email)
                .claim("type", tipo)
                .issuedAt(agora)
                .expiration(new Date(agora.getTime() + durationMs))
                .signWith(key)
                .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }

    public long getAccessExpirationSeconds() {
        return accessMs / 1000;
    }
}
