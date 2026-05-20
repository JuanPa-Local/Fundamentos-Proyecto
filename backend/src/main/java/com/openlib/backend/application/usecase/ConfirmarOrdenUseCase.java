package com.openlib.backend.application.usecase;

import com.openlib.backend.domain.order.*;
import com.openlib.backend.domain.order.exception.CarritoVacioException;
import com.openlib.backend.domain.order.exception.SesionCheckoutIncompletaException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ConfirmarOrdenUseCase {

    private final CheckoutSessionGateway checkoutSessionGateway;
    private final CarritoRepository carritoRepository;
    private final OrderService orderService; // Usa la lógica actual de crear orden
    private final BibliotecaRepository bibliotecaRepository;

    public ConfirmarOrdenUseCase(CheckoutSessionGateway checkoutSessionGateway,
                                 CarritoRepository carritoRepository,
                                 OrderService orderService,
                                 BibliotecaRepository bibliotecaRepository) {
        this.checkoutSessionGateway = checkoutSessionGateway;
        this.carritoRepository = carritoRepository;
        this.orderService = orderService;
        this.bibliotecaRepository = bibliotecaRepository;
    }

    @Transactional
    public void ejecutar(UUID buyerId) {
        SesionCheckout sesion = checkoutSessionGateway.buscarPorBuyerId(buyerId)
                .orElseThrow(() -> new SesionCheckoutIncompletaException("Sesión no encontrada. Vuelve a iniciar."));

        if (sesion.getDireccion() == null || sesion.getMetodoPago() == null) {
            throw new SesionCheckoutIncompletaException("Faltan datos en el checkout (Dirección o Método de Pago).");
        }

        Carrito carrito = carritoRepository.buscarPorBuyerId(buyerId)
                .orElseThrow(() -> new CarritoVacioException("No tienes un carrito activo."));

        if (carrito.estaVacio()) {
            throw new CarritoVacioException("El carrito está vacío. No se puede proceder.");
        }

        // Crear una orden por cada libro
        for (ItemCarrito item : carrito.getItems()) {
            // Delega a la capa existente para crear la orden y aplicar descuentos
            orderService.createOrder(buyerId, item.getLibroId());
            // Simulamos agregarlo a la biblioteca (US-019 lo implementará bien)
            // bibliotecaRepository.agregar(buyerId, item.getLibroId());
        }

        // Limpiar el estado
        carritoRepository.eliminar(buyerId);
        checkoutSessionGateway.eliminar(buyerId);
    }
}
