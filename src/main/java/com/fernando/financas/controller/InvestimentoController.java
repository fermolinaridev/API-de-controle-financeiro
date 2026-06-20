package com.fernando.financas.controller;

import com.fernando.financas.dto.CarteiraResponse;
import com.fernando.financas.dto.InvestimentoRequest;
import com.fernando.financas.dto.InvestimentoResponse;
import com.fernando.financas.service.InvestimentoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/investimentos")
@RequiredArgsConstructor
@Tag(name = "Investimentos")
public class InvestimentoController {

    private final InvestimentoService service;

    @GetMapping
    public CarteiraResponse carteira() {
        return service.listarCarteira();
    }

    @PostMapping
    public ResponseEntity<InvestimentoResponse> criar(@Valid @RequestBody InvestimentoRequest req) {
        InvestimentoResponse criado = service.criar(req);
        return ResponseEntity.created(URI.create("/api/investimentos/" + criado.id())).body(criado);
    }

    @PutMapping("/{id}")
    public InvestimentoResponse atualizar(@PathVariable Long id, @Valid @RequestBody InvestimentoRequest req) {
        return service.atualizar(id, req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
