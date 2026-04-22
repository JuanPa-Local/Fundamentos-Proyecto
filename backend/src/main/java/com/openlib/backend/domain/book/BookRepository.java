package com.openlib.backend.domain.book;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, UUID> {

    // Spring genera el SQL automáticamente leyendo el nombre del método
    List<Book> findByTitleContainingIgnoreCase(String title);
    List<Book> findByAuthorContainingIgnoreCase(String author);
    List<Book> findByCategory(String category);
    Optional<Book> findByIsbn(String isbn);
}