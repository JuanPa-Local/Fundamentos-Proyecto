package com.openlib.backend.domain.book;

import com.openlib.backend.domain.book.exception.IsbnDuplicadoException;
import com.openlib.backend.domain.book.exception.LibroNoEncontradoException;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
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
        existing.setCategory(updatedBook.getCategory());
        existing.setCoverUrl(updatedBook.getCoverUrl());
        existing.setPrice(updatedBook.getPrice());
        return bookRepository.save(existing);
    }

    public void deleteBook(UUID id) {
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
        return bookRepository.findDistinctCategories();
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
        return bookRepository.findByCategoriesExcluding(categories, excludeIds, limit);
    }
}