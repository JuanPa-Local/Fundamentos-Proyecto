package com.openlib.backend.domain.order;

import com.openlib.backend.domain.order.exception.LibroYaEnBibliotecaException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Biblioteca {
    private UUID buyerId;

    @Builder.Default
    private List<LibroBiblioteca> libros = new ArrayList<>();

    public void agregar(LibroBiblioteca libro) {
        if (contiene(libro.getLibroId())) {
            throw new LibroYaEnBibliotecaException("El libro ya se encuentra en tu biblioteca.");
        }
        libros.add(libro);
    }

    public boolean contiene(UUID libroId) {
        return libros.stream().anyMatch(l -> l.getLibroId().equals(libroId));
    }

    public boolean estaVacia() {
        return libros.isEmpty();
    }
}
