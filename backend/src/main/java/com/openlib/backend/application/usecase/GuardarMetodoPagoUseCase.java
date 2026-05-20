package com.openlib.backend.application.usecase;

import com.openlib.backend.domain.order.CheckoutSessionGateway;
import com.openlib.backend.domain.order.MetodoPago;
import com.openlib.backend.domain.order.SesionCheckout;
import com.openlib.backend.domain.order.exception.MetodoPagoInvalidoException;
import com.openlib.backend.domain.order.exception.SesionCheckoutExpiradaException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class GuardarMetodoPagoUseCase {

    private final CheckoutSessionGateway checkoutSessionGateway;

    public GuardarMetodoPagoUseCase(CheckoutSessionGateway checkoutSessionGateway) {
        this.checkoutSessionGateway = checkoutSessionGateway;
    }

    public void ejecutar(UUID buyerId, String metodoPagoStr) {
        MetodoPago metodoPago;
        try {
            metodoPago = MetodoPago.valueOf(metodoPagoStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new MetodoPagoInvalidoException("El método de pago no es válido.");
        }

        SesionCheckout sesion = checkoutSessionGateway.buscarPorBuyerId(buyerId)
                .orElseThrow(() -> new SesionCheckoutExpiradaException("Tu sesión de pago ha expirado. Vuelve al carrito."));

        sesion.setMetodoPago(metodoPago.name());
        checkoutSessionGateway.guardar(sesion, 30);
    }
}
