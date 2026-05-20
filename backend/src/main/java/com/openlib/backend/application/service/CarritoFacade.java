package com.openlib.backend.application.service;

import com.openlib.backend.domain.book.Book;
import com.openlib.backend.domain.book.BookRepository;
import com.openlib.backend.domain.order.BibliotecaRepository;
import com.openlib.backend.domain.order.Carrito;
import com.openlib.backend.domain.order.CarritoRepository;
import com.openlib.backend.domain.order.exception.LibroNoDisponibleException;
import com.openlib.backend.domain.order.exception.LibroYaAdquiridoException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class CarritoFacade {

    private final CarritoRepository carritoRepository;
    private final BibliotecaRepository bibliotecaRepository;
    private final BookRepository bookRepository;

    public CarritoFacade(CarritoRepository carritoRepository,
                         BibliotecaRepository bibliotecaRepository,
                         BookRepository bookRepository) {
        this.carritoRepository = carritoRepository;
        this.bibliotecaRepository = bibliotecaRepository;
        this.bookRepository = bookRepository;
    }

    public void agregarItem(UUID buyerId, UUID libroId) {
        Book book = bookRepository.findById(libroId)
                .orElseThrow(() -> new LibroNoDisponibleException("El libro no existe."));

        if (!"APPROVED".equalsIgnoreCase(book.getStatus())) {
            throw new LibroNoDisponibleException("El libro no está disponible para la compra.");
        }

        if (bibliotecaRepository.existeEnBiblioteca(buyerId, libroId)) {
            throw new LibroYaAdquiridoException("El libro ya se encuentra en tu biblioteca.");
        }

        Carrito carrito = carritoRepository.buscarPorBuyerId(buyerId).orElseGet(() -> {
            Carrito nuevoCarrito = new Carrito();
            nuevoCarrito.setBuyerId(buyerId);
            nuevoCarrito.setFechaCreacion(LocalDateTime.now());
            nuevoCarrito.setFechaUltimaModificacion(LocalDateTime.now());
            return nuevoCarrito;
        });

        carrito.agregarItem(libroId, book.getTitle());
        carritoRepository.guardar(carrito, 7);
    }

    public void eliminarItem(UUID buyerId, UUID libroId) {
        carritoRepository.buscarPorBuyerId(buyerId).ifPresent(carrito -> {
            carrito.eliminarItem(libroId);
            carritoRepository.guardar(carrito, 7);
        });
    }

    public Optional<Carrito> verCarrito(UUID buyerId) {
        return carritoRepository.buscarPorBuyerId(buyerId);
    }
}
