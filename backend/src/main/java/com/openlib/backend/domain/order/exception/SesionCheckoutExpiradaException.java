package com.openlib.backend.domain.order.exception;

public class SesionCheckoutExpiradaException extends RuntimeException {
    public SesionCheckoutExpiradaException(String message) {
        super(message);
    }
}
