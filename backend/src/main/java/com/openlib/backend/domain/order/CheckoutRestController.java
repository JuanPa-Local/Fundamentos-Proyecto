package com.openlib.backend.domain.order;

import com.openlib.backend.application.service.CheckoutFacade;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/checkout")
public class CheckoutRestController {

    private final CheckoutFacade checkoutFacade;

    public CheckoutRestController(CheckoutFacade checkoutFacade) {
        this.checkoutFacade = checkoutFacade;
    }

    @GetMapping("/{buyerId}/address")
    public ResponseEntity<DireccionFacturacion> getAddress(@PathVariable UUID buyerId) {
        DireccionFacturacion direccion = checkoutFacade.obtenerDireccionPrerrellena(buyerId);
        if (direccion != null) {
            return ResponseEntity.ok(direccion);
        }
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{buyerId}/address")
    public ResponseEntity<Void> saveAddress(@PathVariable UUID buyerId, @RequestBody Map<String, String> body) {
        checkoutFacade.guardarDireccion(
                buyerId,
                body.get("calle"),
                body.get("ciudad"),
                body.get("departamento"),
                body.get("pais")
        );
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{buyerId}/payment")
    public ResponseEntity<Void> savePayment(@PathVariable UUID buyerId, @RequestBody Map<String, String> body) {
        checkoutFacade.guardarMetodoPago(buyerId, body.get("metodo"));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{buyerId}/confirm")
    public ResponseEntity<Void> confirmOrder(@PathVariable UUID buyerId) {
        checkoutFacade.confirmarOrden(buyerId);
        return ResponseEntity.ok().build();
    }
}
