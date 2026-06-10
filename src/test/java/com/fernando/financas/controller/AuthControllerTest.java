package com.fernando.financas.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fernando.financas.dto.LoginRequest;
import com.fernando.financas.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    @Test
    void registerRetornaTokenEPermiteAcesso() throws Exception {
        var req = new RegisterRequest("Maria", "maria@test.com", "senha123");
        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.email").value("maria@test.com"));
    }

    @Test
    void registerComEmailDuplicadoRetorna422() throws Exception {
        var req = new RegisterRequest("Carlos", "dup@test.com", "senha123");
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(req))).andExpect(status().isOk());

        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(req))).andExpect(status().isUnprocessableEntity());
    }

    @Test
    void loginComCredenciaisInvalidasRetorna401() throws Exception {
        var req = new LoginRequest("naoexiste@test.com", "qualquer");
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refreshRetornaNovoAccessToken() throws Exception {
        var reg = new RegisterRequest("Refresh", "refresh@test.com", "senha123");
        String body = mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(reg)))
                .andReturn().getResponse().getContentAsString();
        String refreshToken = json.readTree(body).get("refreshToken").asText();

        mvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty());
    }

    @Test
    void refreshComAccessTokenFalha() throws Exception {
        var reg = new RegisterRequest("Wrong", "wrong@test.com", "senha123");
        String body = mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(reg)))
                .andReturn().getResponse().getContentAsString();
        String accessToken = json.readTree(body).get("accessToken").asText();

        mvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + accessToken + "\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logoutInvalidaRefreshToken() throws Exception {
        var reg = new RegisterRequest("Logout", "logout@test.com", "senha123");
        String body = mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(reg)))
                .andReturn().getResponse().getContentAsString();
        String refreshToken = json.readTree(body).get("refreshToken").asText();
        String refreshJson = "{\"refreshToken\":\"" + refreshToken + "\"}";

        mvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshJson))
                .andExpect(status().isNoContent());

        mvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshJson))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refreshTokenSoPodeSerUsadoUmaVez() throws Exception {
        var reg = new RegisterRequest("Rotacao", "rotacao@test.com", "senha123");
        String body = mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(reg)))
                .andReturn().getResponse().getContentAsString();
        String refreshToken = json.readTree(body).get("refreshToken").asText();
        String refreshJson = "{\"refreshToken\":\"" + refreshToken + "\"}";

        // primeiro uso: OK e devolve um refresh diferente
        String segundo = mvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshJson))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String novoRefresh = json.readTree(segundo).get("refreshToken").asText();
        if (novoRefresh.equals(refreshToken)) throw new AssertionError("refresh não rotacionou");

        // reuso do token antigo: revogado
        mvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshJson))
                .andExpect(status().isUnauthorized());

        // o novo continua válido
        mvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + novoRefresh + "\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void logoutComTokenInvalidoNaoQuebra() throws Exception {
        mvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"abc.def.ghi\"}"))
                .andExpect(status().isNoContent());
    }

    @Test
    void rotaProtegidaSemTokenRetorna401Ou403() throws Exception {
        mvc.perform(get("/api/categorias"))
                .andExpect(result -> {
                    int s = result.getResponse().getStatus();
                    if (s != 401 && s != 403) throw new AssertionError("esperado 401/403, foi " + s);
                });
    }
}
