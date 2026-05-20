package com.openlib.backend.domain.book;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

// US-024: Repositorio de reseñas
@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {

    List<Review> findByBookId(UUID bookId);
    List<Review> findByBookIdAndStatus(UUID bookId, String status);
    List<Review> findByUserId(UUID userId);
    boolean existsByUserIdAndBookId(UUID userId, UUID bookId);

    // US-025: Reseñas reportadas para moderación
    List<Review> findByStatus(String status);
}
