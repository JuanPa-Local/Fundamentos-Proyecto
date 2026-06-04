package com.openlib.backend.domain.book;

import com.openlib.backend.domain.book.exception.IsbnDuplicadoException;
import com.openlib.backend.domain.book.exception.LibroNoEncontradoException;
import com.openlib.backend.domain.order.OrderRepository;
import com.openlib.backend.domain.order.FavoriteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;
    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final FavoriteRepository favoriteRepository;

    public BookService(BookRepository bookRepository, CategoryRepository categoryRepository,
                       ReviewRepository reviewRepository, OrderRepository orderRepository,
                       FavoriteRepository favoriteRepository) {
        this.bookRepository = bookRepository;
        this.categoryRepository = categoryRepository;
        this.reviewRepository = reviewRepository;
        this.orderRepository = orderRepository;
        this.favoriteRepository = favoriteRepository;
    }

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    // US-012: Detalle del libro
    public Book getBookById(UUID id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new LibroNoEncontradoException(id));
    }

    public List<Book> searchByTitle(String title) {
        return bookRepository.findByTitleContainingIgnoreCase(title);
    }

    // US-009: Publicación de libro con validación de ISBN
    public Book createBook(Book book) {
        if (book.getIsbn() != null && !book.getIsbn().isBlank() && bookRepository.existsByIsbn(book.getIsbn())) {
            throw new IsbnDuplicadoException(book.getIsbn());
        }
        return bookRepository.save(book);
    }

    public Book updateBook(UUID id, Book updatedBook) {
        Book existing = getBookById(id);
        existing.setTitle(updatedBook.getTitle());
        existing.setAuthor(updatedBook.getAuthor());
        existing.setIsbn(updatedBook.getIsbn());
        existing.setDescription(updatedBook.getDescription());
        existing.setCategories(updatedBook.getCategories());
        existing.setCoverUrl(updatedBook.getCoverUrl());
        existing.setPrice(updatedBook.getPrice());
        return bookRepository.save(existing);
    }

    @Transactional
    public void deleteBook(UUID id) {
        // Eliminar favoritos del libro
        favoriteRepository.deleteAll(favoriteRepository.findByBookId(id));
        // Eliminar reseñas del libro
        reviewRepository.deleteAll(reviewRepository.findByBookId(id));
        // Desvincular órdenes que referencian este libro
        orderRepository.findByBookId(id).forEach(order -> {
            order.setBook(null);
            orderRepository.save(order);
        });
        bookRepository.deleteById(id);
    }

    public long countBooks() {
        return bookRepository.count();
    }

    // US-009: Libros por vendedor
    public List<Book> getBooksBySeller(String sellerEmail) {
        return bookRepository.findBySellerEmail(sellerEmail);
    }

    public void deleteBookByIsbn(String isbn) {
        bookRepository.findByIsbn(isbn)
                .ifPresent(bookRepository::delete);
    }

    // US-009: Verificar si ya existe un libro con ese ISBN
    public boolean existsByIsbn(String isbn) {
        return bookRepository.existsByIsbn(isbn);
    }

    // US-010: Búsqueda del catálogo por término en título, autor o ISBN
    public List<Book> searchCatalog(String termino) {
        if (termino == null || termino.isBlank()) {
            return bookRepository.findByStatus("APROBADO");
        }
        return bookRepository.searchByTermino(termino);
    }

    // US-011: Búsqueda con filtros avanzados (término + categoría)
    public List<Book> searchCatalogWithFilters(String termino, String category) {
        boolean hasTermino = termino != null && !termino.isBlank();
        boolean hasCategory = category != null && !category.isBlank();

        if (hasTermino && hasCategory) {
            return bookRepository.searchByTerminoAndCategory(termino, category);
        } else if (hasCategory) {
            return bookRepository.findByCategoryAndStatus(category, "APROBADO");
        } else if (hasTermino) {
            return bookRepository.searchByTermino(termino);
        } else {
            return bookRepository.findByStatus("APROBADO");
        }
    }

    // US-013: Obtener todas las categorías distintas
    public List<String> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(Category::getName)
                .toList();
    }

    public Category createCategory(String name) {
        if (categoryRepository.existsByName(name)) {
            throw new IllegalArgumentException("La categoría ya existe");
        }
        Category category = new Category();
        category.setName(name);
        return categoryRepository.save(category);
    }

    // US-014: Listar libros según su estado (PENDIENTE, APROBADO, RECHAZADO)
    public List<Book> getBooksByStatus(String status) {
        return bookRepository.findByStatus(status);
    }

    // US-014: Aprobar libro
    public Book approveBook(UUID id) {
        Book book = getBookById(id);
        book.approve();
        return bookRepository.save(book);
    }

    // US-014: Rechazar libro con motivo
    public Book rejectBook(UUID id, String reason) {
        Book book = getBookById(id);
        book.reject(reason);
        return bookRepository.save(book);
    }

    // US-023: Obtener libros más populares (por descargas)
    public List<Book> getMostPopularBooks(int limit) {
        return bookRepository.findMostPopular(limit);
    }

    // US-023: Obtener categorías de un conjunto de libros
    public List<String> getCategoriesByBookIds(List<UUID> bookIds) {
        return bookRepository.findDistinctCategoriesByIds(bookIds);
    }

    // US-023: Obtener recomendaciones por categorías excluyendo libros ya adquiridos
    public List<Book> getRecommendations(List<String> categories, List<UUID> excludeIds, int limit) {
        if (categories == null || categories.isEmpty()) return new java.util.ArrayList<>();
        return bookRepository.findByCategoriesExcluding(
            categories,
            excludeIds == null || excludeIds.isEmpty() ? List.of(UUID.randomUUID()) : excludeIds,
            org.springframework.data.domain.PageRequest.of(0, limit)
        );
    }
}