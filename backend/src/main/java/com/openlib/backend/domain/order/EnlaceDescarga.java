package com.openlib.backend.domain.order;

import com.openlib.backend.domain.order.exception.EnlaceExpiradoException;
import com.openlib.backend.domain.order.exception.EnlaceYaUsadoException;

import java.time.LocalDateTime;
import java.util.UUID;

public class EnlaceDescarga {
    private UUID id;
    private UUID buyerId;
    private UUID libroId;
    private String token;
    private LocalDateTime expiracion;
    private boolean usado;

    public EnlaceDescarga() {}

    public EnlaceDescarga(UUID id, UUID buyerId, UUID libroId, String token, LocalDateTime expiracion, boolean usado) {
        this.id = id;
        this.buyerId = buyerId;
        this.libroId = libroId;
        this.token = token;
        this.expiracion = expiracion;
        this.usado = usado;
    }

    public static EnlaceDescarga generar(UUID buyerId, UUID libroId, String token) {
        return new EnlaceDescarga(
                UUID.randomUUID(),
                buyerId,
                libroId,
                token,
                LocalDateTime.now().plusMinutes(15),
                false
        );
    }

    public void marcarComoUsado() {
        if (this.usado) {
            throw new EnlaceYaUsadoException("El enlace de descarga ya ha sido utilizado.");
        }
        this.usado = true;
    }

    public boolean estaVigente() {
        return LocalDateTime.now().isBefore(this.expiracion) && !this.usado;
    }

    public void validar() {
        if (this.usado) {
            throw new EnlaceYaUsadoException("El enlace de descarga ya ha sido utilizado.");
        }
        if (!estaVigente()) {
            throw new EnlaceExpiradoException("El enlace de descarga ha expirado.");
        }
    }

    public UUID getId() {
        return id;
    }

    public UUID getBuyerId() {
        return buyerId;
    }

    public UUID getLibroId() {
        return libroId;
    }

    public String getToken() {
        return token;
    }

    public LocalDateTime getExpiracion() {
        return expiracion;
    }

    public boolean isUsado() {
        return usado;
    }
}
