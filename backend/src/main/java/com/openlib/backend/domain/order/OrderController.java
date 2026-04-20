package com.openlib.backend.domain.order;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

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
    public List<Order> getByUser(@PathVariable UUID userId) {
        return orderService.getOrdersByUser(userId);
    }
}