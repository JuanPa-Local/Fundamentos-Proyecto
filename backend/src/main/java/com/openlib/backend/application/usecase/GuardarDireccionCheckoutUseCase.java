package com.openlib.backend.application.usecase;

import com.openlib.backend.domain.order.CarritoRepository;
import com.openlib.backend.domain.order.CheckoutSessionGateway;
import com.openlib.backend.domain.order.DireccionFacturacion;
import com.openlib.backend.domain.order.SesionCheckout;
import com.openlib.backend.domain.order.exception.CarritoVacioException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class GuardarDireccionCheckoutUseCase {

    private final CheckoutSessionGateway checkoutSessionGateway;
    private final CarritoRepository carritoRepository;

    public GuardarDireccionCheckoutUseCase(CheckoutSessionGateway checkoutSessionGateway, 
                                           CarritoRepository carritoRepository) {
        this.checkoutSessionGateway = checkoutSessionGateway;
        this.carritoRepository = carritoRepository;
    }

    public void ejecutar(UUID buyerId, String calle, String ciudad, String departamento, String pais) {
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
}
