package com.fernando.financas.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fernando.financas.dto.RegisterRequest;
import org.junit.jupiter.api.BeforeEach;
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
class CategoriaControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    String tokenA;
    String tokenB;

    @BeforeEach
    void registrarUsuarios() throws Exception {
        tokenA = registrar("catA" + System.nanoTime() + "@test.com");
        tokenB = registrar("catB" + System.nanoTime() + "@test.com");
    }

    private String registrar(String email) throws Exception {
        var req = new RegisterRequest("User", email, "senha123");
        String body = mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(req)))
                .andReturn().getResponse().getContentAsString();
        return json.readTree(body).get("accessToken").asText();
    }

    private long criarCategoria(String token, String nome) throws Exception {
        String body = mvc.perform(post("/api/categorias")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"" + nome + "\",\"tipo\":\"DESPESA\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return json.readTree(body).get("id").asLong();
    }

    @Test
    void categoriaDoSistemaNaoPodeSerEditadaNemExcluida() throws Exception {
        // pega uma categoria do sistema (seedada na migration, doSistema=true)
        String cats = mvc.perform(get("/api/categorias").header("Authorization", "Bearer " + tokenA))
                .andReturn().getResponse().getContentAsString();
        long sistemaId = -1;
        for (JsonNode c : json.readTree(cats)) {
            if (c.get("doSistema").asBoolean()) { sistemaId = c.get("id").asLong(); break; }
        }
        if (sistemaId == -1) throw new AssertionError("nenhuma categoria do sistema seedada");

        mvc.perform(put("/api/categorias/" + sistemaId)
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Hackeada\",\"tipo\":\"DESPESA\"}"))
                .andExpect(status().isUnprocessableEntity());

        mvc.perform(delete("/api/categorias/" + sistemaId)
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void usuarioNaoVeNemAlteraCategoriaDeOutro() throws Exception {
        long idDeB = criarCategoria(tokenB, "Pets do B");

        // A não enxerga a categoria do B na listagem
        mvc.perform(get("/api/categorias").header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == " + idDeB + ")]").isEmpty());

        // A não consegue editar nem excluir (404: não vaza existência)
        mvc.perform(put("/api/categorias/" + idDeB)
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Invadida\",\"tipo\":\"DESPESA\"}"))
                .andExpect(status().isNotFound());

        mvc.perform(delete("/api/categorias/" + idDeB)
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isNotFound());
    }

    @Test
    void usuarioGerenciaPropriaCategoria() throws Exception {
        long id = criarCategoria(tokenA, "Minha categoria");

        mvc.perform(put("/api/categorias/" + id)
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Renomeada\",\"tipo\":\"DESPESA\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Renomeada"));

        mvc.perform(delete("/api/categorias/" + id)
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isNoContent());
    }
}
