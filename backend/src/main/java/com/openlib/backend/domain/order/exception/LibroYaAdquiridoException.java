package com.openlib.backend.domain.order.exception;

public class LibroYaAdquiridoException extends RuntimeException {
    public LibroYaAdquiridoException() {
        super("El libro ya se encuentra en tu biblioteca.");
    }

    public LibroYaAdquiridoException(String message) {
        super(message);
    }
}
