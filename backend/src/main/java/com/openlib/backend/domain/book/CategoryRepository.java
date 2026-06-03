package com.openlib.backend.domain.book;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
    boolean existsByName(String name);
}
