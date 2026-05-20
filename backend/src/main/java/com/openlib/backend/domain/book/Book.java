package com.openlib.backend.domain.book;

import com.openlib.backend.domain.book.exception.LibroNoEnEstadoPendienteException;
import com.openlib.backend.domain.book.exception.MotivoRechazoObligatorioException;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "books")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String author;

    private String isbn;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String category;

    private String filePath;

    private String coverUrl;

    // US-009: Estado del libro en el flujo de aprobación
    @Builder.Default
    @Column(length = 30)
    private String status = "PENDIENTE";

    // US-009: Referencia al vendedor que publicó el libro
    private String sellerEmail;

    // US-014: Razón de rechazo (nulo si no fue rechazado)
    @Column(columnDefinition = "TEXT")
    private String rejectionReason;

    // US-028: Precio del libro
    @Builder.Default
    private Double price = 0.0;

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    private int downloadCount = 0;

    // US-024: Calificación promedio y número de reseñas
    @Builder.Default
    private double averageRating = 0.0;

    @Builder.Default
    private int reviewCount = 0;

    // Getters y Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public int getDownloadCount() { return downloadCount; }
    public void setDownloadCount(int downloadCount) { this.downloadCount = downloadCount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getSellerEmail() { return sellerEmail; }
    public void setSellerEmail(String sellerEmail) { this.sellerEmail = sellerEmail; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public double getAverageRating() { return averageRating; }
    public void setAverageRating(double averageRating) { this.averageRating = averageRating; }

    public int getReviewCount() { return reviewCount; }
    public void setReviewCount(int reviewCount) { this.reviewCount = reviewCount; }

    // US-014: Comportamientos de aprobación del libro (excepciones tipadas)
    public void approve() {
        if (!"PENDIENTE".equals(this.status)) {
            throw new LibroNoEnEstadoPendienteException(this.status);
        }
        this.status = "APROBADO";
    }

    public void reject(String reason) {
        if (!"PENDIENTE".equals(this.status)) {
            throw new LibroNoEnEstadoPendienteException(this.status);
        }
        if (reason == null || reason.isBlank()) {
            throw new MotivoRechazoObligatorioException();
        }
        this.status = "RECHAZADO";
        this.rejectionReason = reason;
    }

    // US-024: Actualizar calificación promedio al recibir una nueva reseña
    public void actualizarCalificacion(double nuevaCalificacion) {
        double totalActual = this.averageRating * this.reviewCount;
        this.reviewCount++;
        this.averageRating = (totalActual + nuevaCalificacion) / this.reviewCount;
    }
}