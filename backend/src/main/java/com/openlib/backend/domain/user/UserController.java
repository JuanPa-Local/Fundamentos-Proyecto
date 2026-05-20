package com.openlib.backend.domain.user;

import com.openlib.backend.domain.user.exception.CredencialesInvalidasException;
import com.openlib.backend.domain.user.exception.CuentaInactivaException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // US-004: Registro de Buyer
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

    // US-005: Login con validación de cuenta activa
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> body) {
        String email    = body.get("email");
        String password = body.get("password");

        try {
            User user = userService.login(email, password);
            return ResponseEntity.ok(Map.of(
                    "email",    user.getEmail(),
                    "fullName", user.getFullName(),
                    "role",     user.getRole().name(),
                    "id",       user.getId().toString()
            ));
        } catch (CredencialesInvalidasException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        } catch (CuentaInactivaException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/hash-test")
    public String hashTest() {
        return userService.generarHash("openlib123");
    }

    // US-006: Registro de Seller
    @PostMapping("/register-seller")
    public ResponseEntity<User> registerSeller(@RequestBody Map<String, String> body) {
        User user = userService.registerSeller(
                body.get("fullName"),
                body.get("email"),
                body.get("password")
        );
        return ResponseEntity.ok(user);
    }

    // US-007: Ver perfil
    @GetMapping("/{id}/profile")
    public ResponseEntity<User> getProfile(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getProfile(id));
    }

    // US-007: Actualizar perfil
    @PutMapping("/{id}/profile")
    public ResponseEntity<User> updateProfile(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        User user = userService.updateProfile(
                id,
                body.get("fullName"),
                body.get("phone"),
                body.get("address")
        );
        return ResponseEntity.ok(user);
    }

    // US-008: Listar usuarios por rol (Admin)
    @GetMapping("/role/{role}")
    public List<User> getByRole(@PathVariable String role) {
        return userService.getUsersByRole(role);
    }

    // US-008: Activar cuenta (Admin)
    @PostMapping("/{id}/activate")
    public User activateUser(@PathVariable UUID id) {
        return userService.activateUser(id);
    }

    // US-008: Desactivar cuenta (Admin)
    @PostMapping("/{id}/deactivate")
    public User deactivateUser(@PathVariable UUID id) {
        return userService.deactivateUser(id);
    }
}