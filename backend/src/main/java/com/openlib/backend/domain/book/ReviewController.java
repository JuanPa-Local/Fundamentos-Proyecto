package com.openlib.backend.domain.book;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;

// US-024 + US-025: Controller de reseñas
@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    // US-024: Crear reseña
    @PostMapping
    public ResponseEntity<Review> create(@RequestBody Map<String, String> body) {
        Review review = reviewService.createReview(
                UUID.fromString(body.get("userId")),
                UUID.fromString(body.get("bookId")),
                Integer.parseInt(body.get("rating")),
                body.get("comment")
        );
        return ResponseEntity.ok(review);
    }

    // US-024: Reseñas de un libro (solo activas)
    @GetMapping("/book/{bookId}")
    public List<Review> getByBook(@PathVariable UUID bookId) {
        return reviewService.getReviewsByBook(bookId);
    }

    // US-025: Reportar reseña
    @PostMapping("/{id}/report")
    public ResponseEntity<Review> report(@PathVariable UUID id) {
        return ResponseEntity.ok(reviewService.reportReview(id));
    }

    // US-025: Ocultar reseña (admin)
    @PostMapping("/{id}/hide")
    public ResponseEntity<Review> hide(@PathVariable UUID id) {
        return ResponseEntity.ok(reviewService.hideReview(id));
    }

    // US-025: Reactivar reseña (admin)
    @PostMapping("/{id}/reactivate")
    public ResponseEntity<Review> reactivate(@PathVariable UUID id) {
        return ResponseEntity.ok(reviewService.reactivateReview(id));
    }

    // US-025: Reseñas reportadas (admin)
    @GetMapping("/reported")
    public List<Review> getReported() {
        return reviewService.getReportedReviews();
    }

    // Admin: Todas las reseñas
    @GetMapping
    public List<Review> getAll() {
        return reviewService.getAllReviews();
    }

    // Admin: Eliminar reseña definitivamente
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        reviewService.deleteReview(id);
        return ResponseEntity.noContent().build();
    }

    // Verificar si un usuario ya reseñó un libro
    @GetMapping("/check")
    public ResponseEntity<Map<String, Boolean>> checkUserReview(
            @RequestParam UUID userId, @RequestParam UUID bookId) {
        boolean hasReview = reviewService.hasUserReviewedBook(userId, bookId);
        return ResponseEntity.ok(Map.of("hasReview", hasReview));
    }
}
