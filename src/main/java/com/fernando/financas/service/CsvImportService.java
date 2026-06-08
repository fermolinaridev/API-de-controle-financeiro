package com.fernando.financas.service;

import com.fernando.financas.dto.ImportarCsvResponse;
import com.fernando.financas.entity.Categoria;
import com.fernando.financas.entity.TipoTransacao;
import com.fernando.financas.entity.Transacao;
import com.fernando.financas.entity.Usuario;
import com.fernando.financas.exception.RecursoNaoEncontradoException;
import com.fernando.financas.repository.CategoriaRepository;
import com.fernando.financas.repository.TransacaoRepository;
import com.fernando.financas.security.UsuarioPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Importa transações de um CSV no formato:
 * descricao,valor,data,tipo,categoria
 *
 * Exemplo:
 *   Salário Junho,5000.00,2026-06-01,RECEITA,Salário
 *   Mercado,234.50,2026-06-03,DESPESA,Alimentação
 *
 * Categorias são resolvidas por nome + tipo (case-insensitive). Se não existir, é criada.
 */
@Service
@RequiredArgsConstructor
public class CsvImportService {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ISO_LOCAL_DATE;

    private final TransacaoRepository transacaoRepository;
    private final CategoriaRepository categoriaRepository;

    @Transactional
    public ImportarCsvResponse importar(InputStream input) {
        Usuario usuario = usuarioAtual();
        List<String> erros = new ArrayList<>();
        int ok = 0;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
            String linha;
            int n = 0;
            while ((linha = reader.readLine()) != null) {
                n++;
                if (linha.isBlank()) continue;
                if (n == 1 && linha.toLowerCase().startsWith("descricao")) continue; // header

                try {
                    Transacao t = parseLinha(linha, usuario);
                    transacaoRepository.save(t);
                    ok++;
                } catch (Exception e) {
                    erros.add("Linha " + n + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            erros.add("Falha ao ler arquivo: " + e.getMessage());
        }

        return new ImportarCsvResponse(ok, erros.size(), erros);
    }

    private Transacao parseLinha(String linha, Usuario usuario) {
        String[] cols = linha.split(",", -1);
        if (cols.length < 5) throw new IllegalArgumentException("esperado 5 colunas, veio " + cols.length);

        String descricao = cols[0].trim();
        BigDecimal valor = parseValor(cols[1].trim());
        if (valor.signum() <= 0) throw new IllegalArgumentException("valor deve ser positivo");
        LocalDate data = LocalDate.parse(cols[2].trim(), DATE);
        TipoTransacao tipo = TipoTransacao.valueOf(cols[3].trim().toUpperCase());
        String catNome = cols[4].trim();
        if (descricao.isEmpty()) throw new IllegalArgumentException("descricao vazia");
        if (catNome.isEmpty()) throw new IllegalArgumentException("categoria vazia");

        Categoria categoria = categoriaRepository.findByNomeIgnoreCaseAndTipo(catNome, tipo)
                .orElseGet(() -> categoriaRepository.save(
                        Categoria.builder().nome(catNome).tipo(tipo).build()));

        return Transacao.builder()
                .descricao(descricao)
                .valor(valor)
                .data(data)
                .tipo(tipo)
                .categoria(categoria)
                .usuario(usuario)
                .build();
    }

    /** Aceita "1234.56" (ponto decimal) e "1.234,56" (formato BR). */
    private BigDecimal parseValor(String raw) {
        try {
            return new BigDecimal(raw);
        } catch (NumberFormatException e) {
            return new BigDecimal(raw.replace(".", "").replace(",", "."));
        }
    }

    private Usuario usuarioAtual() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UsuarioPrincipal up) return up.usuario();
        throw new RecursoNaoEncontradoException("Usuário autenticado não encontrado");
    }
}
