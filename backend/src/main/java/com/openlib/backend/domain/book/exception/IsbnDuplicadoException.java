package com.openlib.backend.domain.book.exception;

public class IsbnDuplicadoException extends RuntimeException {
    public IsbnDuplicadoException(String isbn) {
        super("Ya existe un libro con el ISBN: " + isbn);
    }
}
