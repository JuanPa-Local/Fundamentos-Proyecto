package com.openlib.backend.domain.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

// US-021: Repositorio de favoritos
@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, UUID> {

    List<Favorite> findByUserId(UUID userId);
    boolean existsByUserIdAndBookId(UUID userId, UUID bookId);
    void deleteByUserIdAndBookId(UUID userId, UUID bookId);
}
