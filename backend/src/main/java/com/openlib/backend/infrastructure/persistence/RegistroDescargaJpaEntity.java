package com.openlib.backend.infrastructure.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "registro_descarga")
@Getter
@Setter
public class RegistroDescargaJpaEntity {
    @Id
    private UUID id;
    private UUID buyerId;
    private UUID libroId;
    private String ipOrigen;
    private LocalDateTime timestamp;
}
