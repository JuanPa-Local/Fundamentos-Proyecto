package com.openlib.backend.domain.book;

import com.openlib.backend.domain.book.exception.LibroNoEnEstadoPendienteException;
import com.openlib.backend.domain.book.exception.MotivoRechazoObligatorioException;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "books")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
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

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "book_categories", joinColumns = @JoinColumn(name = "book_id"))
    @Column(name = "category_name")
    @Builder.Default
    private java.util.List<String> categories = new java.util.ArrayList<>();

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

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder.Default
    private Integer downloadCount = 0;

    // US-024: Calificación promedio y número de reseñas
    @Builder.Default
    private Double averageRating = 0.0;

    @Builder.Default
    private Integer reviewCount = 0;

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

    public java.util.List<String> getCategories() { return categories; }
    public void setCategories(java.util.List<String> categories) { this.categories = categories; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Integer getDownloadCount() { return downloadCount; }
    public void setDownloadCount(Integer downloadCount) { this.downloadCount = downloadCount != null ? downloadCount : 0; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getSellerEmail() { return sellerEmail; }
    public void setSellerEmail(String sellerEmail) { this.sellerEmail = sellerEmail; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public Double getAverageRating() { return averageRating; }
    public void setAverageRating(Double averageRating) { this.averageRating = averageRating != null ? averageRating : 0.0; }

    public Integer getReviewCount() { return reviewCount; }
    public void setReviewCount(Integer reviewCount) { this.reviewCount = reviewCount != null ? reviewCount : 0; }

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
        double currentRating = this.averageRating != null ? this.averageRating : 0.0;
        int currentCount = this.reviewCount != null ? this.reviewCount : 0;
        double totalActual = currentRating * currentCount;
        this.reviewCount = currentCount + 1;
        this.averageRating = (totalActual + nuevaCalificacion) / this.reviewCount;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}