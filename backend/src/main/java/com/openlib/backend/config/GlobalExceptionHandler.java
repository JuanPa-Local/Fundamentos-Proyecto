package com.openlib.backend.config;

import com.openlib.backend.domain.book.exception.*;
import com.openlib.backend.domain.user.exception.*;
import com.openlib.backend.domain.order.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.Map;

// US-029: Manejador global de excepciones — centraliza respuestas de error
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmailDuplicadoException.class)
    public ResponseEntity<Map<String, String>> handleEmailDuplicado(EmailDuplicadoException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(CredencialesInvalidasException.class)
    public ResponseEntity<Map<String, String>> handleCredenciales(CredencialesInvalidasException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(CuentaInactivaException.class)
    public ResponseEntity<Map<String, String>> handleCuentaInactiva(CuentaInactivaException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(UsuarioNoEncontradoException.class)
    public ResponseEntity<Map<String, String>> handleUsuarioNoEncontrado(UsuarioNoEncontradoException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(LibroNoEncontradoException.class)
    public ResponseEntity<Map<String, String>> handleLibroNoEncontrado(LibroNoEncontradoException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(IsbnDuplicadoException.class)
    public ResponseEntity<Map<String, String>> handleIsbnDuplicado(IsbnDuplicadoException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(LibroNoEnEstadoPendienteException.class)
    public ResponseEntity<Map<String, String>> handleLibroNoPendiente(LibroNoEnEstadoPendienteException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(MotivoRechazoObligatorioException.class)
    public ResponseEntity<Map<String, String>> handleMotivoRechazo(MotivoRechazoObligatorioException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(LibroYaAdquiridoException.class)
    public ResponseEntity<Map<String, String>> handleLibroYaAdquirido(LibroYaAdquiridoException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(OrdenNoEncontradaException.class)
    public ResponseEntity<Map<String, String>> handleOrdenNoEncontrada(OrdenNoEncontradaException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(LibroYaEnBibliotecaException.class)
    public ResponseEntity<Map<String, String>> handleLibroYaEnBiblioteca(LibroYaEnBibliotecaException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(ResenaYaExisteException.class)
    public ResponseEntity<Map<String, String>> handleResenaYaExiste(ResenaYaExisteException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(ResenaNoEncontradaException.class)
    public ResponseEntity<Map<String, String>> handleResenaNoEncontrada(ResenaNoEncontradaException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(LibroYaEnFavoritosException.class)
    public ResponseEntity<Map<String, String>> handleLibroYaEnFavoritos(LibroYaEnFavoritosException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(LibroNoEnFavoritosException.class)
    public ResponseEntity<Map<String, String>> handleLibroNoEnFavoritos(LibroNoEnFavoritosException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
    }

    // Catch-all para excepciones no manejadas
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleGeneric(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Error interno del servidor"));
    }
}
