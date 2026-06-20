package com.fernando.financas.entity;

/**
 * Classe do ativo na carteira. Define qual endpoint de cotação usar:
 * CRIPTO consulta o endpoint de criptomoedas; os demais usam o de ações da B3.
 */
public enum TipoAtivo {
    ACAO,
    FII,
    BDR,
    CRIPTO,
    OUTRO
}
