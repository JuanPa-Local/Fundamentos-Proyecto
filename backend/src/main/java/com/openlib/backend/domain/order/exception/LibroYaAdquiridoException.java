package com.openlib.backend.domain.order.exception;

public class LibroYaAdquiridoException extends RuntimeException {
    public LibroYaAdquiridoException() {
        super("El usuario ya tiene este libro.");
    }
}
