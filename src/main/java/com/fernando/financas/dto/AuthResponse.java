package com.fernando.financas.dto;

public record AuthResponse(String token, long expiresIn, String nome, String email) {}
