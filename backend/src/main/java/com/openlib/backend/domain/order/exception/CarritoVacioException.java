package com.openlib.backend.domain.order.exception;

public class CarritoVacioException extends RuntimeException {
    public CarritoVacioException(String message) {
        super(message);
    }
}
