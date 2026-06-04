package com.openlib.backend.domain.order;

import com.openlib.backend.domain.book.Book;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // US-018: Crear orden
    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody Map<String, String> body) {
        String userIdStr = body.get("userId");
        String bookIdStr = body.get("bookId");
        if (bookIdStr == null && body.containsKey("libroId")) {
            bookIdStr = body.get("libroId");
        }
        
        if (userIdStr == null || bookIdStr == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Falta userId o bookId en la petición. Recibido: " + body));
        }

        Order order = orderService.createOrder(
                UUID.fromString(userIdStr),
                UUID.fromString(bookIdStr)
        );
        return ResponseEntity.ok(order);
    }

    // US-022: Historial de órdenes paginado
    @GetMapping("/history/{userId}")
    public ResponseEntity<Page<Order>> getHistory(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(orderService.getOrderHistory(userId, page, size));
    }

    // US-022: Detalle de una orden
    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable UUID id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    // US-019: Biblioteca del usuario (libros adquiridos)
    @GetMapping("/library/{userId}")
    public List<Book> getLibrary(@PathVariable UUID userId) {
        return orderService.getLibrary(userId);
    }

    // US-019: Verificar si un libro está en la biblioteca
    @GetMapping("/library/check")
    public ResponseEntity<Map<String, Boolean>> isInLibrary(
            @RequestParam UUID userId, @RequestParam UUID bookId) {
        return ResponseEntity.ok(Map.of("inLibrary", orderService.isInLibrary(userId, bookId)));
    }

    // Listado por usuario
    @GetMapping("/user/{userId}")
    public List<Order> getByUser(@PathVariable UUID userId) {
        return orderService.getOrdersByUser(userId);
    }

    // Todas las órdenes (admin)
    @GetMapping
    public List<Order> getAll() {
        return orderService.getAllOrders();
    }

    // US-026: Estadísticas de ventas del seller
    @GetMapping("/seller/{sellerEmail}")
    public List<Order> getBySeller(@PathVariable String sellerEmail) {
        return orderService.getOrdersBySeller(sellerEmail);
    }
}