package com.openlib.backend.domain.order;

import com.openlib.backend.domain.book.Book;
import com.openlib.backend.domain.book.BookRepository;
import com.openlib.backend.domain.user.User;
import com.openlib.backend.domain.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

// US-021: Servicio de favoritos
@Service
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    public FavoriteService(FavoriteRepository favoriteRepository,
                          UserRepository userRepository,
                          BookRepository bookRepository) {
        this.favoriteRepository = favoriteRepository;
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
    }

    // US-021: Agregar a favoritos
    @Transactional
    public Favorite addFavorite(UUID userId, UUID bookId) {
        if (favoriteRepository.existsByUserIdAndBookId(userId, bookId)) {
            throw new RuntimeException("El libro ya está en favoritos.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Libro no encontrado"));

        Favorite favorite = new Favorite();
        favorite.setUser(user);
        favorite.setBook(book);
        return favoriteRepository.save(favorite);
    }

    // US-021: Eliminar de favoritos
    @Transactional
    public void removeFavorite(UUID userId, UUID bookId) {
        if (!favoriteRepository.existsByUserIdAndBookId(userId, bookId)) {
            throw new RuntimeException("El libro no está en favoritos.");
        }
        favoriteRepository.deleteByUserIdAndBookId(userId, bookId);
    }

    // US-021: Listar favoritos
    @Transactional(readOnly = true)
    public List<Favorite> getFavorites(UUID userId) {
        List<Favorite> favorites = favoriteRepository.findByUserId(userId);
        // Forzar carga de relaciones lazy
        favorites.forEach(f -> {
            if (f.getBook() != null) f.getBook().getTitle();
        });
        return favorites;
    }

    // US-021: Verificar si un libro está en favoritos
    public boolean isFavorite(UUID userId, UUID bookId) {
        return favoriteRepository.existsByUserIdAndBookId(userId, bookId);
    }
}
