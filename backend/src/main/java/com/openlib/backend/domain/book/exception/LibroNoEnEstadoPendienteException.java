package com.openlib.backend.domain.book.exception;

public class LibroNoEnEstadoPendienteException extends RuntimeException {
    public LibroNoEnEstadoPendienteException(String estadoActual) {
        super("Solo se pueden aprobar/rechazar libros en estado PENDIENTE. Estado actual: " + estadoActual);
    }
}
