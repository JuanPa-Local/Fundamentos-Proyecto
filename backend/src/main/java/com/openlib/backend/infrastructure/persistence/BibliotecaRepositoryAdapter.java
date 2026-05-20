package com.openlib.backend.infrastructure.persistence;

import com.openlib.backend.domain.book.Book;
import com.openlib.backend.domain.book.BookRepository;
import com.openlib.backend.domain.order.BibliotecaRepository;
import com.openlib.backend.domain.order.LibroBiblioteca;
import com.openlib.backend.domain.order.Order;
import com.openlib.backend.domain.order.OrderRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adaptador de BibliotecaRepository que usa las órdenes completadas como fuente de verdad.
 * Un libro está en la biblioteca de un buyer si existe una orden COMPLETED con ese libro.
 */
@Repository
public class BibliotecaRepositoryAdapter implements BibliotecaRepository {

    private final OrderRepository orderRepository;
    private final BookRepository bookRepository;

    public BibliotecaRepositoryAdapter(OrderRepository orderRepository, BookRepository bookRepository) {
        this.orderRepository = orderRepository;
        this.bookRepository = bookRepository;
    }

    @Override
    public boolean existeEnBiblioteca(UUID buyerId, UUID libroId) {
        return orderRepository.existsByUserIdAndBookId(buyerId, libroId);
    }

    @Override
    public void agregar(UUID buyerId, UUID libroId) {
        // La biblioteca se alimenta implícitamente de las órdenes completadas.
        // Este método existe por si se necesita agregar un libro directamente (e.g. regalo).
        // En el flujo normal, crear la orden ya agrega el libro a la biblioteca.
    }

    @Override
    public List<LibroBiblioteca> buscarPorBuyerId(UUID buyerId) {
        List<Order> orders = orderRepository.findByUserIdAndStatus(buyerId, "COMPLETED");
        return orders.stream()
                .filter(o -> o.getBook() != null)
                .map(o -> {
                    Book book = o.getBook();
                    return LibroBiblioteca.builder()
                            .libroId(book.getId())
                            .titulo(book.getTitle())
                            .autor(book.getAuthor())
                            .portadaUrl(book.getCoverUrl())
                            .fechaAdquisicion(o.getOrderedAt())
                            .build();
                })
                .collect(Collectors.toList());
    }
}
