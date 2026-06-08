package com.fernando.financas.controller;

import com.fernando.financas.dto.ImportarCsvResponse;
import com.fernando.financas.dto.ResumoResponse;
import com.fernando.financas.dto.TransacaoRequest;
import com.fernando.financas.dto.TransacaoResponse;
import com.fernando.financas.service.CsvImportService;
import com.fernando.financas.service.TransacaoService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;

@RestController
@RequestMapping("/api/transacoes")
@RequiredArgsConstructor
@Tag(name = "Transações")
public class TransacaoController {

    private final TransacaoService service;
    private final CsvImportService csvImportService;

    @PostMapping
    public ResponseEntity<TransacaoResponse> criar(@Valid @RequestBody TransacaoRequest req) {
        TransacaoResponse criada = service.criar(req);
        return ResponseEntity.created(URI.create("/api/transacoes/" + criada.id())).body(criada);
    }

    @GetMapping
    public Page<TransacaoResponse> listar(
            @Parameter(description = "Mês (1-12)") @RequestParam(required = false) Integer mes,
            @Parameter(description = "Ano (ex: 2026)") @RequestParam(required = false) Integer ano,
            @RequestParam(required = false) Long categoriaId,
            Pageable pageable) {
        return service.listar(mes, ano, categoriaId, pageable);
    }

    @PutMapping("/{id}")
    public TransacaoResponse atualizar(@PathVariable Long id, @Valid @RequestBody TransacaoRequest req) {
        return service.atualizar(id, req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/resumo")
    public ResumoResponse resumo() {
        return service.resumoMesAtual();
    }

    @PostMapping(value = "/importar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ImportarCsvResponse importarCsv(@RequestParam("arquivo") MultipartFile arquivo) throws IOException {
        return csvImportService.importar(arquivo.getInputStream());
    }
}
