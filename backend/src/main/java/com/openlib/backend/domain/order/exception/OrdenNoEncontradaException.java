package com.openlib.backend.domain.order.exception;

import java.util.UUID;

public class OrdenNoEncontradaException extends RuntimeException {
    public OrdenNoEncontradaException(UUID id) {
        super("Orden no encontrada: " + id);
    }
}
