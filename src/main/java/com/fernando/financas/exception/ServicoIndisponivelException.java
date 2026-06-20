package com.fernando.financas.exception;

/** Falha ao consultar um serviço externo (ex.: API de cotações brapi). */
public class ServicoIndisponivelException extends RuntimeException {
    public ServicoIndisponivelException(String message) {
        super(message);
    }
}
