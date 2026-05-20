package com.openlib.backend.application.usecase;

import com.openlib.backend.domain.order.Carrito;
import com.openlib.backend.domain.order.CarritoRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class VerCarritoUseCase {

    private final CarritoRepository carritoRepository;

    public VerCarritoUseCase(CarritoRepository carritoRepository) {
        this.carritoRepository = carritoRepository;
    }

    public Optional<Carrito> ejecutar(UUID buyerId) {
        return carritoRepository.buscarPorBuyerId(buyerId);
    }
}
