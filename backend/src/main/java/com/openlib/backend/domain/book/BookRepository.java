package com.openlib.backend.domain.book;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, UUID> {

    List<Book> findByTitleContainingIgnoreCase(String title);
    List<Book> findByAuthorContainingIgnoreCase(String author);
    List<Book> findByCategory(String category);
    Optional<Book> findByIsbn(String isbn);

    // US-009: Libros por vendedor
    List<Book> findBySellerEmail(String sellerEmail);

    // US-010: Búsqueda del catálogo por término en título, autor o ISBN
    @Query("SELECT b FROM Book b WHERE " +
           "LOWER(b.title) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
           "LOWER(b.author) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
           "b.isbn LIKE CONCAT('%', :termino, '%')")
    List<Book> searchByTermino(@Param("termino") String termino);

    // US-011: Búsqueda con filtro de categoría adicional
    @Query("SELECT b FROM Book b WHERE b.category = :category AND (" +
           "LOWER(b.title) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
           "LOWER(b.author) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
           "b.isbn LIKE CONCAT('%', :termino, '%'))")
    List<Book> searchByTerminoAndCategory(@Param("termino") String termino, @Param("category") String category);

    // US-011: Filtrar solo por categoría
    List<Book> findByCategoryAndStatus(String category, String status);

    // US-013: Categorías distintas
    @Query("SELECT DISTINCT b.category FROM Book b WHERE b.category IS NOT NULL ORDER BY b.category")
    List<String> findDistinctCategories();

    // US-014: Filtrar por estado (PENDIENTE, APROBADO, RECHAZADO)
    List<Book> findByStatus(String status);
    boolean existsByIsbn(String isbn);

    // US-023: Libros más populares (por descargas)
    @Query(value = "SELECT * FROM books WHERE status = 'APROBADO' ORDER BY download_count DESC LIMIT :limit",
           nativeQuery = true)
    List<Book> findMostPopular(@Param("limit") int limit);

    // US-023: Categorías de un conjunto de libros
    @Query("SELECT DISTINCT b.category FROM Book b WHERE b.id IN :ids AND b.category IS NOT NULL")
    List<String> findDistinctCategoriesByIds(@Param("ids") List<UUID> ids);

    // US-023: Recomendaciones por categorías excluyendo libros ya adquiridos
    @Query(value = "SELECT * FROM books WHERE status = 'APROBADO' AND category IN :categories " +
                   "AND id NOT IN :excludeIds ORDER BY download_count DESC LIMIT :limit",
           nativeQuery = true)
    List<Book> findByCategoriesExcluding(@Param("categories") List<String> categories,
                                         @Param("excludeIds") List<UUID> excludeIds,
                                         @Param("limit") int limit);

    // US-024: Libros por calificación
    @Query("SELECT b FROM Book b WHERE b.status = 'APROBADO' ORDER BY b.averageRating DESC")
    List<Book> findByRating();
}