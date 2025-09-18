package com.sistema.sistema.infrastructure.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

public class BusinessException extends RuntimeException {

    @Getter
    private final HttpStatus status;
    private final String message;

    public BusinessException(HttpStatus status, String message) {
        super(message);
        this.status = status;
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
