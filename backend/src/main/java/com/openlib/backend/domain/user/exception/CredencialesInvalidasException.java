package com.openlib.backend.domain.user.exception;

public class CredencialesInvalidasException extends RuntimeException {
    public CredencialesInvalidasException() {
        super("Credenciales inválidas: email o contraseña incorrectos.");
    }
}
