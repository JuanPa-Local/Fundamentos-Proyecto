package com.openlib.backend.domain.order;

import com.openlib.backend.domain.book.Book;
import com.openlib.backend.domain.book.BookRepository;
import com.openlib.backend.domain.user.User;
import com.openlib.backend.domain.user.UserRepository;
import com.openlib.backend.domain.order.exception.LibroYaAdquiridoException;
import com.openlib.backend.domain.order.exception.OrdenNoEncontradaException;
import com.openlib.backend.domain.order.strategy.DiscountStrategy;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final DiscountStrategy discountStrategy;
    private final ApplicationEventPublisher eventPublisher;

    public OrderService(OrderRepository orderRepository,
                        UserRepository userRepository,
                        BookRepository bookRepository,
                        DiscountStrategy discountStrategy,
                        ApplicationEventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
        this.discountStrategy = discountStrategy;
        this.eventPublisher = eventPublisher;
    }

    // US-018: Crear orden
    public Order createOrder(UUID userId, UUID bookId) {
        if (orderRepository.existsByUserIdAndBookId(userId, bookId)) {
            throw new LibroYaAdquiridoException();
        }

        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        Book book = bookRepository.findById(bookId).orElseThrow(() -> new RuntimeException("Libro no encontrado"));

        Order order = new Order();
        order.setUser(user);
        order.setBook(book);
        order.setStatus("COMPLETED");

        // Usar precio real del libro o precio base si no tiene
        double basePrice = book.getPrice() != null && book.getPrice() > 0 ? book.getPrice() : 100.0;
        double finalPrice = discountStrategy.calculate(basePrice);
        order.setTotalPrice(finalPrice);

        Order savedOrder = orderRepository.save(order);

        // Publicar evento para Observer pattern
        eventPublisher.publishEvent(new OrderCompletedEvent(this, book));

        return savedOrder;
    }

    // US-022: Historial de órdenes del usuario (con paginación)
    @Transactional(readOnly = true)
    public Page<Order> getOrderHistory(UUID userId, int page, int size) {
        Page<Order> orders = orderRepository.findByUserIdOrderByOrderedAtDesc(
                userId, PageRequest.of(page, Math.min(size, 50)));
        // Forzar carga de relaciones lazy
        orders.getContent().forEach(o -> {
            if (o.getBook() != null) o.getBook().getTitle();
        });
        return orders;
    }

    @Transactional(readOnly = true)
    public List<Order> getOrdersByUser(UUID userId) {
        List<Order> orders = orderRepository.findByUserId(userId);
        orders.forEach(o -> {
            if (o.getBook() != null) o.getBook().getTitle();
        });
        return orders;
    }

    // US-022: Detalle de una orden específica
    @Transactional(readOnly = true)
    public Order getOrderById(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrdenNoEncontradaException(orderId));
        // Forzar carga lazy
        if (order.getUser() != null) order.getUser().getFullName();
        if (order.getBook() != null) order.getBook().getTitle();
        return order;
    }

    // US-019: Biblioteca — libros adquiridos por el buyer
    @Transactional(readOnly = true)
    public List<Book> getLibrary(UUID userId) {
        List<Order> completedOrders = orderRepository.findByUserIdAndStatus(userId, "COMPLETED");
        return completedOrders.stream()
                .map(Order::getBook)
                .filter(book -> book != null)
                .collect(Collectors.toList());
    }

    // US-019: Verificar si un libro está en la biblioteca del buyer
    public boolean isInLibrary(UUID userId, UUID bookId) {
        return orderRepository.existsByUserIdAndBookId(userId, bookId);
    }

    @Transactional(readOnly = true)
    public List<Order> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        orders.forEach(o -> {
            if (o.getUser() != null) o.getUser().getFullName();
            if (o.getBook() != null) o.getBook().getTitle();
        });
        return orders;
    }

    // US-026: Obtener estadísticas del seller
    @Transactional(readOnly = true)
    public List<Order> getOrdersBySeller(String sellerEmail) {
        List<Order> orders = orderRepository.findByBookSellerEmail(sellerEmail);
        orders.forEach(o -> {
            if (o.getBook() != null) o.getBook().getTitle();
            if (o.getUser() != null) o.getUser().getFullName();
        });
        return orders;
    }
}