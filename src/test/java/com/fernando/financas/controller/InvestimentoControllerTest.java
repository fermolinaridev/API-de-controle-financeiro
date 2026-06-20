package com.fernando.financas.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fernando.financas.client.AwesomeApiClient;
import com.fernando.financas.client.BrapiClient;
import com.fernando.financas.client.YahooFinanceClient;
import com.fernando.financas.dto.CotacaoResponse;
import com.fernando.financas.dto.InvestimentoRequest;
import com.fernando.financas.dto.RegisterRequest;
import com.fernando.financas.entity.TipoAtivo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class InvestimentoControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @MockitoBean BrapiClient brapi;
    @MockitoBean AwesomeApiClient awesome;
    @MockitoBean YahooFinanceClient yahoo;

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
    void adicionarELerCarteiraComRendimento() throws Exception {
        when(brapi.cotacaoAcao("PETR4")).thenReturn(
                new CotacaoResponse("PETR4", "Petrobras", new BigDecimal("40.0"), null, "BRL"));

        var req = new InvestimentoRequest("petr4", TipoAtivo.ACAO,
                new BigDecimal("10"), new BigDecimal("30"));
        mvc.perform(post("/api/investimentos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ticker").value("PETR4"))
                .andExpect(jsonPath("$.valorInvestido").value(300.00))
                .andExpect(jsonPath("$.rendimento").value(100.00));

        mvc.perform(get("/api/investimentos").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resumo.totalInvestido").value(300.00))
                .andExpect(jsonPath("$.itens[0].ticker").value("PETR4"));
    }

    @Test
    void quantidadeInvalidaRetorna400() throws Exception {
        var req = new InvestimentoRequest("PETR4", TipoAtivo.ACAO,
                new BigDecimal("-1"), new BigDecimal("30"));
        mvc.perform(post("/api/investimentos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }
}
