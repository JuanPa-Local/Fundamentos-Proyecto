package com.openlib.backend.domain.order;

import java.time.LocalDateTime;
import java.util.UUID;

public class RegistroDescarga {
    private final UUID buyerId;
    private final UUID libroId;
    private final String ipOrigen;
    private final LocalDateTime timestamp;

    public RegistroDescarga(UUID buyerId, UUID libroId, String ipOrigen, LocalDateTime timestamp) {
        this.buyerId = buyerId;
        this.libroId = libroId;
        this.ipOrigen = ipOrigen;
        this.timestamp = timestamp;
    }

    public UUID getBuyerId() {
        return buyerId;
    }

    public UUID getLibroId() {
        return libroId;
    }

    public String getIpOrigen() {
        return ipOrigen;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
