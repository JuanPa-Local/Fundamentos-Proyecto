package com.openlib.backend.domain.book.exception;

import java.util.UUID;

public class LibroNoEncontradoException extends RuntimeException {
    public LibroNoEncontradoException(UUID id) {
        super("Libro no encontrado: " + id);
    }
}
