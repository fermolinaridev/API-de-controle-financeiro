package com.fernando.financas.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fernando.financas.dto.RegisterRequest;
import com.fernando.financas.dto.TransacaoRequest;
import com.fernando.financas.entity.TipoTransacao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TransacaoControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    String token;

    @BeforeEach
    void registrarELogar() throws Exception {
        String email = "user" + System.nanoTime() + "@test.com";
        var req = new RegisterRequest("User", email, "senha123");
        String body = mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(req)))
                .andReturn().getResponse().getContentAsString();
        token = json.readTree(body).get("accessToken").asText();
    }

    @Test
    void criarListarEResumir() throws Exception {
        // pega uma categoria RECEITA (seedada)
        String cats = mvc.perform(get("/api/categorias").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        long categoriaId = -1;
        for (JsonNode c : json.readTree(cats)) {
            if ("RECEITA".equals(c.get("tipo").asText())) { categoriaId = c.get("id").asLong(); break; }
        }
        if (categoriaId == -1) throw new AssertionError("nenhuma categoria RECEITA seedada");

        var req = new TransacaoRequest("Salário", new BigDecimal("3000"),
                LocalDate.now().withDayOfMonth(1), TipoTransacao.RECEITA, categoriaId);
        mvc.perform(post("/api/transacoes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.descricao").value("Salário"));

        mvc.perform(get("/api/transacoes/resumo").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalReceitas").value(3000.00));
    }

    @Test
    void criarComTipoIncompativelComCategoriaRetorna422() throws Exception {
        String cats = mvc.perform(get("/api/categorias").header("Authorization", "Bearer " + token))
                .andReturn().getResponse().getContentAsString();
        long catReceitaId = -1;
        for (JsonNode c : json.readTree(cats)) {
            if ("RECEITA".equals(c.get("tipo").asText())) { catReceitaId = c.get("id").asLong(); break; }
        }

        var req = new TransacaoRequest("Compra", new BigDecimal("100"),
                LocalDate.now(), TipoTransacao.DESPESA, catReceitaId);
        mvc.perform(post("/api/transacoes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(req)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void valorNegativoRetorna400() throws Exception {
        var req = new TransacaoRequest("X", new BigDecimal("-1"), LocalDate.now(), TipoTransacao.DESPESA, 1L);
        mvc.perform(post("/api/transacoes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }
}
