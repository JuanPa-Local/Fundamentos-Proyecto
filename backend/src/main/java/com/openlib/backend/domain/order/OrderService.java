package com.openlib.backend.domain.order;

import com.openlib.backend.domain.book.Book;
import com.openlib.backend.domain.book.BookRepository;
import com.openlib.backend.domain.user.User;
import com.openlib.backend.domain.user.UserRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    public OrderService(OrderRepository orderRepository,
                        UserRepository userRepository,
                        BookRepository bookRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
    }

    public Order createOrder(UUID userId, UUID bookId) {
        // Verificar que no compró el mismo libro antes
        if (orderRepository.existsByUserIdAndBookId(userId, bookId)) {
            throw new RuntimeException("El usuario ya tiene este libro");
        }

        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        Book book = bookRepository.findById(bookId).orElseThrow(() -> new RuntimeException("Libro no encontrado"));

        Order order = new Order();
        order.setUser(user);
        order.setBook(book);
        order.setStatus("COMPLETED");
        return orderRepository.save(order);
    }

    @org.springframework.transaction.annotation.Transactional
    public List<Order> getOrdersByUser(UUID userId) {
        List<Order> orders = orderRepository.findByUserId(userId);
        orders.forEach(o -> {
            if (o.getBook() != null) o.getBook().getTitle();
        });
        return orders;
    }

    @org.springframework.transaction.annotation.Transactional
    public List<Order> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        // Forzar carga de relaciones lazy antes de cerrar la sesión
        orders.forEach(o -> {
            if (o.getUser() != null) o.getUser().getFullName();
            if (o.getBook() != null) o.getBook().getTitle();
        });
        return orders;
    }
}