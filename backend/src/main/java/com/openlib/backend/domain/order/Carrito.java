package com.openlib.backend.domain.order;

import com.openlib.backend.domain.order.exception.ItemNoEncontradoException;
import com.openlib.backend.domain.order.exception.LibroYaEnCarritoException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Carrito {
    private UUID buyerId;
    
    @Builder.Default
    private List<ItemCarrito> items = new ArrayList<>();
    
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaUltimaModificacion;

    public void agregarItem(UUID libroId, String titulo) {
        if (contieneLibro(libroId)) {
            throw new LibroYaEnCarritoException("El libro ya se encuentra en el carrito.");
        }
        items.add(new ItemCarrito(libroId, titulo));
        actualizarFechaModificacion();
    }

    public void eliminarItem(UUID libroId) {
        boolean removed = items.removeIf(item -> item.getLibroId().equals(libroId));
        if (!removed) {
            throw new ItemNoEncontradoException("El item no fue encontrado en el carrito.");
        }
        actualizarFechaModificacion();
    }

    public void vaciar() {
        items.clear();
        actualizarFechaModificacion();
    }

    public boolean contieneLibro(UUID libroId) {
        return items.stream().anyMatch(item -> item.getLibroId().equals(libroId));
    }

    public boolean estaVacio() {
        return items.isEmpty();
    }

    private void actualizarFechaModificacion() {
        this.fechaUltimaModificacion = LocalDateTime.now();
    }
}
