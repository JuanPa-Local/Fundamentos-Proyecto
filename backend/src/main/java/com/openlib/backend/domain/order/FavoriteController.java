package com.openlib.backend.domain.order;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;

// US-021: Controller de favoritos
@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    // US-021: Agregar a favoritos
    @PostMapping
    public ResponseEntity<Favorite> add(@RequestBody Map<String, String> body) {
        Favorite fav = favoriteService.addFavorite(
                UUID.fromString(body.get("userId")),
                UUID.fromString(body.get("bookId"))
        );
        return ResponseEntity.ok(fav);
    }

    // US-021: Eliminar de favoritos
    @DeleteMapping
    public ResponseEntity<Void> remove(@RequestParam UUID userId, @RequestParam UUID bookId) {
        favoriteService.removeFavorite(userId, bookId);
        return ResponseEntity.noContent().build();
    }

    // US-021: Listar favoritos de un usuario
    @GetMapping("/{userId}")
    public List<Favorite> list(@PathVariable UUID userId) {
        return favoriteService.getFavorites(userId);
    }

    // US-021: Verificar si un libro está en favoritos
    @GetMapping("/check")
    public ResponseEntity<Map<String, Boolean>> check(@RequestParam UUID userId, @RequestParam UUID bookId) {
        boolean isFav = favoriteService.isFavorite(userId, bookId);
        return ResponseEntity.ok(Map.of("isFavorite", isFav));
    }
}
