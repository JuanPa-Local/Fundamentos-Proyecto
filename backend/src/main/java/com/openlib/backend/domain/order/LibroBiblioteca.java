package com.openlib.backend.domain.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LibroBiblioteca {
    private UUID libroId;
    private String titulo;
    private String autor;
    private String portadaUrl;
    private LocalDateTime fechaAdquisicion;
}
