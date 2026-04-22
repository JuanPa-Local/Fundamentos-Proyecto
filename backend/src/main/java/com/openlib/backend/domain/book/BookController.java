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

    // GET /api/books  o  GET /api/books?title=java
    @GetMapping
    public List<Book> getAll(@RequestParam(required = false) String title) {
        if (title != null && !title.isBlank()) {
            return bookService.searchByTitle(title);
        }
        return bookService.getAllBooks();
    }

    // GET /api/books/{id}
    @GetMapping("/{id}")
    public Book getById(@PathVariable UUID id) {
        return bookService.getBookById(id);
    }

    // POST /api/books
    @PostMapping
    public Book create(@RequestBody Book book) {
        return bookService.createBook(book);
    }

    // PUT /api/books/{id}
    @PutMapping("/{id}")
    public Book update(@PathVariable UUID id, @RequestBody Book book) {
        return bookService.updateBook(id, book);
    }

    // DELETE /api/books/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }

    // GET /api/books/count
    @GetMapping("/count")
    public Map<String, Long> count() {
        return Map.of("total", bookService.countBooks());
    }

    // GET /api/books/my?email=seller@openlib.com
    @GetMapping("/my")
    public List<Book> getMyBooks(@RequestParam String email) {
        return bookService.getBooksByEmail(email);
    }

    // DELETE /api/books/isbn/{isbn}
    @DeleteMapping("/isbn/{isbn}")
    public ResponseEntity<Void> deleteByIsbn(@PathVariable String isbn) {
        bookService.deleteBookByIsbn(isbn);
        return ResponseEntity.noContent().build();
    }
}