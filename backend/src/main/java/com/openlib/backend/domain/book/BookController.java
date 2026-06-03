package com.openlib.backend.domain.book;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    // GET /api/books
    @GetMapping
    public List<Book> getAll() {
        return bookService.getAllBooks();
    }

    // US-012: GET /api/books/{id} — Detalle del libro
    @GetMapping("/{id}")
    public ResponseEntity<Book> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(bookService.getBookById(id));
    }

    // POST /api/books
    @PostMapping
    public ResponseEntity<Book> create(@RequestBody Book book) {
        return ResponseEntity.ok(bookService.createBook(book));
    }

    // PUT /api/books/{id}
    @PutMapping("/{id}")
    public ResponseEntity<Book> update(@PathVariable UUID id, @RequestBody Book book) {
        return ResponseEntity.ok(bookService.updateBook(id, book));
    }

    // DELETE /api/books/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }

    // US-010: Búsqueda del catálogo
    @GetMapping("/catalog")
    public List<Book> searchCatalog(@RequestParam(required = false) String q) {
        return bookService.searchCatalog(q);
    }

    // US-011: Búsqueda con filtros (término + categoría)
    @GetMapping("/catalog/filter")
    public List<Book> searchCatalogFiltered(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category) {
        return bookService.searchCatalogWithFilters(q, category);
    }

    // US-013: Listar categorías disponibles
    @GetMapping("/categories")
    public List<String> getCategories() {
        return bookService.getAllCategories();
    }

    // US-014: Listar libros por estado (PENDIENTE, APROBADO, RECHAZADO)
    @GetMapping("/status/{status}")
    public List<Book> getByStatus(@PathVariable String status) {
        return bookService.getBooksByStatus(status);
    }

    // US-009: Libros por vendedor
    @GetMapping("/seller/{sellerEmail}")
    public List<Book> getBySeller(@PathVariable String sellerEmail) {
        return bookService.getBooksBySeller(sellerEmail);
    }

    // US-014: Aprobar libro
    @PostMapping("/{id}/approve")
    public ResponseEntity<Book> approve(@PathVariable UUID id) {
        return ResponseEntity.ok(bookService.approveBook(id));
    }

    // US-014: Rechazar libro
    @PostMapping("/{id}/reject")
    public ResponseEntity<Book> reject(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(bookService.rejectBook(id, body.get("reason")));
    }

    // US-023: Libros más populares
    @GetMapping("/popular")
    public List<Book> getPopular(@RequestParam(defaultValue = "10") int limit) {
        return bookService.getMostPopularBooks(limit);
    }

    // Admin: Crear categoría
    @PostMapping("/categories")
    public ResponseEntity<Category> createCategory(@RequestBody Map<String, String> body) {
        Category category = bookService.createCategory(body.get("name"));
        return ResponseEntity.ok(category);
    }
}