package com.sprintjava.service;

/**
 * Erro na comunicação com o serviço Python de análise (indisponibilidade,
 * timeout ou resposta com status de erro).
 */
public class AnalisePythonException extends Exception {

    public AnalisePythonException(String message) {
        super(message);
    }

    public AnalisePythonException(String message, Throwable cause) {
        super(message, cause);
    }
}
