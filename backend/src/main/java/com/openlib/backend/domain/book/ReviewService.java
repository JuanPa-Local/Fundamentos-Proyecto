package com.openlib.backend.domain.book;

import com.openlib.backend.domain.book.exception.LibroNoEncontradoException;
import com.openlib.backend.domain.book.exception.ResenaNoEncontradaException;
import com.openlib.backend.domain.book.exception.ResenaYaExisteException;
import com.openlib.backend.domain.user.User;
import com.openlib.backend.domain.user.UserRepository;
import com.openlib.backend.domain.user.exception.UsuarioNoEncontradoException;
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
            throw new ResenaYaExisteException("Ya has reseñado este libro.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsuarioNoEncontradoException(userId));
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new LibroNoEncontradoException(bookId));

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
                .orElseThrow(() -> new ResenaNoEncontradaException(reviewId));
        review.reportar();
        return reviewRepository.save(review);
    }

    // US-025: Ocultar reseña (admin)
    @Transactional
    public Review hideReview(UUID reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResenaNoEncontradaException(reviewId));
        review.ocultar();
        return reviewRepository.save(review);
    }

    // US-025: Reactivar reseña (admin)
    @Transactional
    public Review reactivateReview(UUID reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResenaNoEncontradaException(reviewId));
        review.reactivar();
        return reviewRepository.save(review);
    }

    // US-025: Listar reseñas reportadas (admin)
    @Transactional(readOnly = true)
    public List<Review> getReportedReviews() {
        return reviewRepository.findByStatus("REPORTADA");
    }

    // Admin: Todas las reseñas
    @Transactional(readOnly = true)
    public List<Review> getAllReviews() {
        return reviewRepository.findAll();
    }

    // Admin: Eliminar reseña definitivamente
    @Transactional
    public void deleteReview(UUID reviewId) {
        reviewRepository.deleteById(reviewId);
    }

    // Verificar si un usuario ya reseñó un libro
    @Transactional(readOnly = true)
    public boolean hasUserReviewedBook(UUID userId, UUID bookId) {
        return reviewRepository.existsByUserIdAndBookId(userId, bookId);
    }
}
