package com.openlib.backend.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface SpringEnlaceDescargaRepository extends JpaRepository<EnlaceDescargaJpaEntity, UUID> {
    Optional<EnlaceDescargaJpaEntity> findByToken(String token);
}
