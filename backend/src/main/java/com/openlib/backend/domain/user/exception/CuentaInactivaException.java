package com.openlib.backend.domain.user.exception;

public class CuentaInactivaException extends RuntimeException {
    public CuentaInactivaException(String email) {
        super("La cuenta está desactivada: " + email);
    }
}
