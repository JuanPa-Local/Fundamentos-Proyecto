package com.openlib.backend.application.service;

import com.openlib.backend.domain.order.*;
import com.openlib.backend.domain.order.exception.CarritoVacioException;
import com.openlib.backend.domain.order.exception.MetodoPagoInvalidoException;
import com.openlib.backend.domain.order.exception.SesionCheckoutExpiradaException;
import com.openlib.backend.domain.order.exception.SesionCheckoutIncompletaException;
import com.openlib.backend.domain.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class CheckoutFacade {

    private final CheckoutSessionGateway checkoutSessionGateway;
    private final CarritoRepository carritoRepository;
    private final OrderService orderService;
    private final BibliotecaRepository bibliotecaRepository;
    private final UserRepository userRepository;

    public CheckoutFacade(CheckoutSessionGateway checkoutSessionGateway,
                          CarritoRepository carritoRepository,
                          OrderService orderService,
                          BibliotecaRepository bibliotecaRepository,
                          UserRepository userRepository) {
        this.checkoutSessionGateway = checkoutSessionGateway;
        this.carritoRepository = carritoRepository;
        this.orderService = orderService;
        this.bibliotecaRepository = bibliotecaRepository;
        this.userRepository = userRepository;
    }

    public void guardarDireccion(UUID buyerId, String calle, String ciudad, String departamento, String pais) {
        carritoRepository.buscarPorBuyerId(buyerId).ifPresentOrElse(carrito -> {
            if (carrito.estaVacio()) {
                throw new CarritoVacioException("El carrito está vacío. No se puede proceder al checkout.");
            }
        }, () -> {
            throw new CarritoVacioException("No tienes un carrito activo.");
        });

        DireccionFacturacion direccion = new DireccionFacturacion(calle, ciudad, departamento, pais);

        SesionCheckout sesion = checkoutSessionGateway.buscarPorBuyerId(buyerId).orElseGet(() -> {
            SesionCheckout nuevaSesion = new SesionCheckout();
            nuevaSesion.setBuyerId(buyerId);
            nuevaSesion.setInicioCheckout(LocalDateTime.now());
            return nuevaSesion;
        });

        sesion.setDireccion(direccion);
        checkoutSessionGateway.guardar(sesion, 30);
    }

    public DireccionFacturacion obtenerDireccionPrerrellena(UUID buyerId) {
        return null;
    }

    public void guardarMetodoPago(UUID buyerId, String metodoPagoStr) {
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

    @Transactional
    public void confirmarOrden(UUID buyerId) {
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

        for (ItemCarrito item : carrito.getItems()) {
            orderService.createOrder(buyerId, item.getLibroId());
        }

        carritoRepository.eliminar(buyerId);
        checkoutSessionGateway.eliminar(buyerId);
    }
}
