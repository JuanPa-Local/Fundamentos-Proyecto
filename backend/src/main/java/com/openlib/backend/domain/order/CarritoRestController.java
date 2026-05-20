package com.openlib.backend.domain.order;

import com.openlib.backend.application.service.CarritoFacade;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/cart")
public class CarritoRestController {

    private final CarritoFacade carritoFacade;

    public CarritoRestController(CarritoFacade carritoFacade) {
        this.carritoFacade = carritoFacade;
    }

    @GetMapping("/{buyerId}")
    public ResponseEntity<Carrito> getCart(@PathVariable UUID buyerId) {
        return carritoFacade.verCarrito(buyerId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @PostMapping("/{buyerId}/items/{libroId}")
    public ResponseEntity<Carrito> addItem(@PathVariable UUID buyerId, @PathVariable UUID libroId) {
        carritoFacade.agregarItem(buyerId, libroId);
        return carritoFacade.verCarrito(buyerId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @DeleteMapping("/{buyerId}/items/{libroId}")
    public ResponseEntity<Void> removeItem(@PathVariable UUID buyerId, @PathVariable UUID libroId) {
        carritoFacade.eliminarItem(buyerId, libroId);
        return ResponseEntity.noContent().build();
    }
}
