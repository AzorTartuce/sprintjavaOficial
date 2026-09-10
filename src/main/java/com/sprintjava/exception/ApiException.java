package com.sprintjava.exception;

import org.springframework.http.HttpStatus;

/**
 * Exceção de negócio com status HTTP associado. Capturada pelo
 * GlobalExceptionHandler e respondida no formato {"detail": "..."} esperado
 * pelo frontend.
 */
public class ApiException extends RuntimeException {

    private final HttpStatus status;

    public ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public ApiException(HttpStatus status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
