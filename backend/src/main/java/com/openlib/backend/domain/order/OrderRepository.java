package com.openlib.backend.domain.order;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    List<Order> findByUserId(UUID userId);
    boolean existsByUserIdAndBookId(UUID userId, UUID bookId);

    // US-022: Historial de órdenes paginado
    Page<Order> findByUserIdOrderByOrderedAtDesc(UUID userId, Pageable pageable);

    // US-019: Libros adquiridos por un buyer (para la biblioteca)
    List<Order> findByUserIdAndStatus(UUID userId, String status);

    // US-026: Estadísticas de ventas por seller
    List<Order> findByBookSellerEmail(String sellerEmail);

    // Para desvincular órdenes al eliminar un libro
    List<Order> findByBookId(UUID bookId);
}