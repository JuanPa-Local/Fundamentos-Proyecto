package com.openlib.backend.domain.order.exception;

public class ItemNoEncontradoException extends RuntimeException {
    public ItemNoEncontradoException(String message) {
        super(message);
    }
}
