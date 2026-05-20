package com.openlib.backend.domain.order;

import java.util.Optional;
import java.util.UUID;

public interface CarritoRepository {
    void guardar(Carrito carrito, int ttlDias);
    Optional<Carrito> buscarPorBuyerId(UUID buyerId);
    void eliminar(UUID buyerId);
}
