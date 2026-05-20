package com.openlib.backend.domain.book;

import com.openlib.backend.domain.user.User;
import com.openlib.backend.domain.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

// US-024 + US-025: Servicio de reseñas y moderación
@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    public ReviewService(ReviewRepository reviewRepository,
                         BookRepository bookRepository,
                         UserRepository userRepository) {
        this.reviewRepository = reviewRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
    }

    // US-024: Crear reseña
    @Transactional
    public Review createReview(UUID userId, UUID bookId, int rating, String comment) {
        if (reviewRepository.existsByUserIdAndBookId(userId, bookId)) {
            throw new RuntimeException("Ya has reseñado este libro.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Libro no encontrado"));

        Review review = new Review();
        review.setUser(user);
        review.setBook(book);
        review.setRating(rating);
        review.setComment(comment);

        Review saved = reviewRepository.save(review);

        // Actualizar calificación promedio del libro
        book.actualizarCalificacion(rating);
        bookRepository.save(book);

        return saved;
    }

    // US-024: Obtener reseñas de un libro (solo activas)
    @Transactional(readOnly = true)
    public List<Review> getReviewsByBook(UUID bookId) {
        return reviewRepository.findByBookIdAndStatus(bookId, "ACTIVA");
    }

    // US-024: Obtener todas las reseñas de un libro (para admin)
    @Transactional(readOnly = true)
    public List<Review> getAllReviewsByBook(UUID bookId) {
        return reviewRepository.findByBookId(bookId);
    }

    // US-025: Reportar reseña
    @Transactional
    public Review reportReview(UUID reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Reseña no encontrada"));
        review.reportar();
        return reviewRepository.save(review);
    }

    // US-025: Ocultar reseña (admin)
    @Transactional
    public Review hideReview(UUID reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Reseña no encontrada"));
        review.ocultar();
        return reviewRepository.save(review);
    }

    // US-025: Reactivar reseña (admin)
    @Transactional
    public Review reactivateReview(UUID reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Reseña no encontrada"));
        review.reactivar();
        return reviewRepository.save(review);
    }

    // US-025: Listar reseñas reportadas (admin)
    @Transactional(readOnly = true)
    public List<Review> getReportedReviews() {
        return reviewRepository.findByStatus("REPORTADA");
    }
}
