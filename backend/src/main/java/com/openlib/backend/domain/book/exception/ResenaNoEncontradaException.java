package com.openlib.backend.domain.book.exception;

import java.util.UUID;

public class ResenaNoEncontradaException extends RuntimeException {
    public ResenaNoEncontradaException(UUID id) {
        super("Reseña no encontrada: " + id);
    }
}
