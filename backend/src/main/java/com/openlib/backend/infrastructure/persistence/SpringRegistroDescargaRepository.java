package com.openlib.backend.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface SpringRegistroDescargaRepository extends JpaRepository<RegistroDescargaJpaEntity, UUID> {
}
