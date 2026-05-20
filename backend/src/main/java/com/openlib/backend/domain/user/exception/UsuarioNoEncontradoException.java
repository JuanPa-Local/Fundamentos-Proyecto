package com.openlib.backend.domain.user.exception;

import java.util.UUID;

public class UsuarioNoEncontradoException extends RuntimeException {
    public UsuarioNoEncontradoException(UUID id) {
        super("Usuario no encontrado: " + id);
    }

    public UsuarioNoEncontradoException(String email) {
        super("Usuario no encontrado con email: " + email);
    }
}
