package com.fernando.financas.service;

import com.fernando.financas.dto.ImportarCsvResponse;
import com.fernando.financas.entity.Categoria;
import com.fernando.financas.entity.TipoTransacao;
import com.fernando.financas.entity.Transacao;
import com.fernando.financas.entity.Usuario;
import com.fernando.financas.exception.RegraNegocioException;
import com.fernando.financas.repository.CategoriaRepository;
import com.fernando.financas.repository.TransacaoRepository;
import com.fernando.financas.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
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
 *   "Almoço, restaurante",45.90,2026-06-03,DESPESA,Alimentação
 *
 * Campos com vírgula devem vir entre aspas duplas. Categorias são resolvidas
 * por nome + tipo (case-insensitive); se não existir, é criada para o usuário.
 */
@Service
@RequiredArgsConstructor
public class CsvImportService {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final int MAX_LINHAS = 5000;

    private final TransacaoRepository transacaoRepository;
    private final CategoriaRepository categoriaRepository;

    @Transactional
    public ImportarCsvResponse importar(InputStream input) {
        Usuario usuario = SecurityUtils.usuarioAtual();
        List<String> erros = new ArrayList<>();
        int ok = 0;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
            String linha;
            int n = 0;
            while ((linha = reader.readLine()) != null) {
                n++;
                if (n > MAX_LINHAS) {
                    throw new RegraNegocioException("Arquivo excede o limite de " + MAX_LINHAS + " linhas");
                }
                if (linha.isBlank()) continue;
                if (n == 1 && linha.toLowerCase().startsWith("descricao")) continue; // header

                try {
                    Transacao t = parseLinha(linha, usuario);
                    transacaoRepository.save(t);
                    ok++;
                } catch (RegraNegocioException | IllegalArgumentException | java.time.DateTimeException e) {
                    erros.add("Linha " + n + ": " + e.getMessage());
                }
            }
        } catch (RegraNegocioException e) {
            throw e;
        } catch (Exception e) {
            erros.add("Falha ao ler arquivo: " + e.getMessage());
        }

        return new ImportarCsvResponse(ok, erros.size(), erros);
    }

    private Transacao parseLinha(String linha, Usuario usuario) {
        List<String> cols = splitCsv(linha);
        if (cols.size() < 5) throw new IllegalArgumentException("esperado 5 colunas, veio " + cols.size());

        String descricao = cols.get(0).trim();
        BigDecimal valor = parseValor(cols.get(1).trim());
        LocalDate data = LocalDate.parse(cols.get(2).trim(), DATE);
        TipoTransacao tipo = parseTipo(cols.get(3).trim());
        String catNome = cols.get(4).trim();

        // validações que o banco recusaria — falhar AQUI evita envenenar a transação
        if (descricao.isEmpty()) throw new IllegalArgumentException("descricao vazia");
        if (descricao.length() > 160) throw new IllegalArgumentException("descricao excede 160 caracteres");
        if (catNome.isEmpty()) throw new IllegalArgumentException("categoria vazia");
        if (catNome.length() > 80) throw new IllegalArgumentException("categoria excede 80 caracteres");
        if (valor.signum() <= 0) throw new IllegalArgumentException("valor deve ser positivo");
        if (valor.precision() - valor.scale() > 12) throw new IllegalArgumentException("valor muito grande");

        Categoria categoria = categoriaRepository
                .findVisivelPorNomeETipo(usuario.getId(), catNome, tipo)
                .orElseGet(() -> categoriaRepository.save(
                        Categoria.builder().nome(catNome).tipo(tipo).usuario(usuario).build()));

        return Transacao.builder()
                .descricao(descricao)
                .valor(valor)
                .data(data)
                .tipo(tipo)
                .categoria(categoria)
                .usuario(usuario)
                .build();
    }

    /** Split com suporte a campos entre aspas duplas ("" escapa aspas internas). */
    private List<String> splitCsv(String linha) {
        List<String> cols = new ArrayList<>();
        StringBuilder atual = new StringBuilder();
        boolean dentroDeAspas = false;
        for (int i = 0; i < linha.length(); i++) {
            char c = linha.charAt(i);
            if (dentroDeAspas) {
                if (c == '"') {
                    if (i + 1 < linha.length() && linha.charAt(i + 1) == '"') {
                        atual.append('"');
                        i++;
                    } else {
                        dentroDeAspas = false;
                    }
                } else {
                    atual.append(c);
                }
            } else if (c == '"') {
                dentroDeAspas = true;
            } else if (c == ',') {
                cols.add(atual.toString());
                atual.setLength(0);
            } else {
                atual.append(c);
            }
        }
        cols.add(atual.toString());
        return cols;
    }

    private TipoTransacao parseTipo(String raw) {
        try {
            return TipoTransacao.valueOf(raw.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("tipo deve ser RECEITA ou DESPESA");
        }
    }

    /** Aceita "1234.56" (ponto decimal) e "1.234,56" (formato BR). */
    private BigDecimal parseValor(String raw) {
        try {
            return new BigDecimal(raw);
        } catch (NumberFormatException e) {
            try {
                return new BigDecimal(raw.replace(".", "").replace(",", "."));
            } catch (NumberFormatException e2) {
                throw new IllegalArgumentException("valor inválido: " + raw);
            }
        }
    }
}
