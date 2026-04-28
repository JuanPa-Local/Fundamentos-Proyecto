package com.openlib.backend.domain.user;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody Map<String, String> body) {
        User user = userService.register(
                body.get("fullName"),
                body.get("email"),
                body.get("password")
        );
        return ResponseEntity.ok(user);
    }

    // GET /api/users/{id}
    @GetMapping("/{id}")
    public User getById(@PathVariable UUID id) {
        return userService.getUserById(id);
    }

    // GET /api/users
    @GetMapping
    public List<User> getAll() {
        return userService.getAllUsers();
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> body) {
        String email    = body.get("email");
        String password = body.get("password");

        return userService.login(email, password)
                .map(user -> ResponseEntity.ok(Map.of(
                        "email",    user.getEmail(),
                        "fullName", user.getFullName(),
                        "role",     user.getRole().name(),
                        "id",       user.getId().toString()
                )))
                .orElse(ResponseEntity.status(401).body(Map.of("error", "Correo o contraseña incorrectos")));
    }

    @GetMapping("/hash-test")
    public String hashTest() {
        return userService.generarHash("openlib123");
    }
}