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
                .andExpect(jsonPath("$.token").isNotEmpty())
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
    void rotaProtegidaSemTokenRetorna401Ou403() throws Exception {
        mvc.perform(get("/api/categorias"))
                .andExpect(result -> {
                    int s = result.getResponse().getStatus();
                    if (s != 401 && s != 403) throw new AssertionError("esperado 401/403, foi " + s);
                });
    }
}
