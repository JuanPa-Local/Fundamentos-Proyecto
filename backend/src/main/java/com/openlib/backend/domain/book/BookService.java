package com.openlib.backend.domain.book;

import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
public class BookService {

    private final BookRepository bookRepository;

    // Spring inyecta el repositorio automáticamente
    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    public Book getBookById(UUID id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Libro no encontrado: " + id));
    }

    public List<Book> searchByTitle(String title) {
        return bookRepository.findByTitleContainingIgnoreCase(title);
    }

    public Book createBook(Book book) {
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
        return bookRepository.save(existing);
    }

    public void deleteBook(UUID id) {
        bookRepository.deleteById(id);
    }

    public long countBooks() {
        return bookRepository.count();
    }

    public List<Book> getBooksByEmail(String email) {
        // Por ahora retorna todos los libros
        // Cuando agregues campo "createdBy" en Book, filtras por email
        return bookRepository.findAll();
    }

    public void deleteBookByIsbn(String isbn) {
        bookRepository.findByIsbn(isbn)
                .ifPresent(bookRepository::delete);
    }
}