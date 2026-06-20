package com.fernando.financas.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fernando.financas.client.AwesomeApiClient;
import com.fernando.financas.client.BrapiClient;
import com.fernando.financas.client.YahooFinanceClient;
import com.fernando.financas.dto.AtivoBuscaResponse;
import com.fernando.financas.dto.CotacaoResponse;
import com.fernando.financas.dto.RegisterRequest;
import com.fernando.financas.exception.ServicoIndisponivelException;
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
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MercadoControllerTest {

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
    void cotacaoRetornaPrecoDoAtivo() throws Exception {
        when(brapi.cotacaoAcao("PETR4")).thenReturn(
                new CotacaoResponse("PETR4", "Petrobras", new BigDecimal("38.8"), new BigDecimal("-0.13"), "BRL"));

        mvc.perform(get("/api/mercado/cotacao/PETR4").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ticker").value("PETR4"))
                .andExpect(jsonPath("$.preco").value(38.8))
                .andExpect(jsonPath("$.nome").value("Petrobras"));
    }

    @Test
    void buscaRetornaListaDeAtivos() throws Exception {
        when(brapi.buscar("PETR")).thenReturn(
                List.of(new AtivoBuscaResponse("PETR4", "PETROBRAS", "x.svg", "stock")));

        mvc.perform(get("/api/mercado/buscar").param("q", "PETR").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].ticker").value("PETR4"))
                .andExpect(jsonPath("$[0].nome").value("PETROBRAS"))
                .andExpect(jsonPath("$[0].logo").value("x.svg"));
    }

    @Test
    void cotacaoIndisponivelRetorna503() throws Exception {
        when(awesome.cotacao("USD-BRL", "USD-BRL", "Dólar Americano"))
                .thenThrow(new ServicoIndisponivelException("Cotação indisponível no momento"));

        mvc.perform(get("/api/mercado/dolar").header("Authorization", "Bearer " + token))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.erro").value("Cotação indisponível no momento"));
    }

    @Test
    void semAutenticacaoBloqueia() throws Exception {
        mvc.perform(get("/api/mercado/cotacao/PETR4"))
                .andExpect(status().isForbidden());
    }
}
