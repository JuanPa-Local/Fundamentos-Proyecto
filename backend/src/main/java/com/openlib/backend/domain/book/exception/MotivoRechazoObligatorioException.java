package com.openlib.backend.domain.book.exception;

public class MotivoRechazoObligatorioException extends RuntimeException {
    public MotivoRechazoObligatorioException() {
        super("El motivo de rechazo es obligatorio.");
    }
}
