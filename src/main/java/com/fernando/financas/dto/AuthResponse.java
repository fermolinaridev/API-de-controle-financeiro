package com.fernando.financas.dto;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        long expiresIn,
        String nome,
        String email
) {}
