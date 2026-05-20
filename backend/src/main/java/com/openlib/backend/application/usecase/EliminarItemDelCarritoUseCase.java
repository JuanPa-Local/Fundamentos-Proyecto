package com.openlib.backend.application.usecase;

import com.openlib.backend.domain.order.Carrito;
import com.openlib.backend.domain.order.CarritoRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class EliminarItemDelCarritoUseCase {

    private final CarritoRepository carritoRepository;

    public EliminarItemDelCarritoUseCase(CarritoRepository carritoRepository) {
        this.carritoRepository = carritoRepository;
    }

    public void ejecutar(UUID buyerId, UUID libroId) {
        carritoRepository.buscarPorBuyerId(buyerId).ifPresent(carrito -> {
            carrito.eliminarItem(libroId);
            carritoRepository.guardar(carrito, 7);
        });
    }
}
