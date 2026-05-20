package com.openlib.backend.domain.order;

import java.util.Optional;
import java.util.UUID;

public interface CheckoutSessionGateway {
    void guardar(SesionCheckout sesion, int ttlMinutos);
    Optional<SesionCheckout> buscarPorBuyerId(UUID buyerId);
    void eliminar(UUID buyerId);
}
