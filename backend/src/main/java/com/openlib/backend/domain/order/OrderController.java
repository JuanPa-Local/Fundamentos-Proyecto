package com.openlib.backend.domain.order;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // GET /api/orders — todas las órdenes para Admin
    @GetMapping
    public List<Map<String, String>> getAll() {
        return orderService.getAllOrders().stream()
                .map(o -> Map.of(
                        "id",        o.getId().toString(),
                        "status",    o.getStatus(),
                        "orderedAt", o.getOrderedAt().toString(),
                        "userName",  o.getUser() != null ? o.getUser().getFullName() : "—",
                        "userEmail", o.getUser() != null ? o.getUser().getEmail()    : "—",
                        "bookTitle", o.getBook() != null ? o.getBook().getTitle()    : "—"
                ))
                .collect(Collectors.toList());
    }

    // POST /api/orders
    @PostMapping
    public ResponseEntity<Order> create(@RequestBody Map<String, String> body) {
        Order order = orderService.createOrder(
                UUID.fromString(body.get("userId")),
                UUID.fromString(body.get("bookId"))
        );
        return ResponseEntity.ok(order);
    }

    // GET /api/orders/user/{userId}
    @GetMapping("/user/{userId}")
    public List<Map<String, String>> getByUser(@PathVariable UUID userId) {
        return orderService.getOrdersByUser(userId).stream()
                .map(o -> Map.of(
                        "id",        o.getId().toString(),
                        "status",    o.getStatus(),
                        "orderedAt", o.getOrderedAt().toString(),
                        "bookTitle", o.getBook() != null ? o.getBook().getTitle()  : "—",
                        "bookId",    o.getBook() != null ? o.getBook().getId().toString() : "—"
                ))
                .collect(Collectors.toList());
    }
}