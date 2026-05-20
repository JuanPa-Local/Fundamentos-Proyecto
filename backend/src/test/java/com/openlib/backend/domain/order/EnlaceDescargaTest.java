package com.openlib.backend.domain.order;

import com.openlib.backend.domain.order.exception.EnlaceExpiradoException;
import com.openlib.backend.domain.order.exception.EnlaceYaUsadoException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class EnlaceDescargaTest {

    @Test
    void generar_debeCrearEnlaceConExpiracionA15Minutos() {
        UUID buyerId = UUID.randomUUID();
        UUID libroId = UUID.randomUUID();
        String token = "t0k3n";

        EnlaceDescarga enlace = EnlaceDescarga.generar(buyerId, libroId, token);

        assertNotNull(enlace.getId());
        assertEquals(buyerId, enlace.getBuyerId());
        assertEquals(libroId, enlace.getLibroId());
        assertEquals(token, enlace.getToken());
        assertFalse(enlace.isUsado());
        
        LocalDateTime ahora = LocalDateTime.now();
        assertTrue(enlace.getExpiracion().isAfter(ahora.plusMinutes(14)));
        assertTrue(enlace.getExpiracion().isBefore(ahora.plusMinutes(16)));
    }

    @Test
    void marcarComoUsado_debeSetearUsadoTrue() {
        EnlaceDescarga enlace = EnlaceDescarga.generar(UUID.randomUUID(), UUID.randomUUID(), "t0k3n");
        enlace.marcarComoUsado();
        assertTrue(enlace.isUsado());
    }

    @Test
    void marcarComoUsado_conEnlaceYaUsado_debeLanzarEnlaceYaUsadoException() {
        EnlaceDescarga enlace = EnlaceDescarga.generar(UUID.randomUUID(), UUID.randomUUID(), "t0k3n");
        enlace.marcarComoUsado();
        
        assertThrows(EnlaceYaUsadoException.class, enlace::marcarComoUsado);
    }

    @Test
    void estaVigente_conEnlaceNuevo_debeRetornarTrue() {
        EnlaceDescarga enlace = EnlaceDescarga.generar(UUID.randomUUID(), UUID.randomUUID(), "t0k3n");
        assertTrue(enlace.estaVigente());
    }

    @Test
    void estaVigente_conEnlaceExpirado_debeRetornarFalse() {
        EnlaceDescarga enlace = new EnlaceDescarga(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "t0k3n", LocalDateTime.now().minusMinutes(1), false);
        assertFalse(enlace.estaVigente());
    }

    @Test
    void validar_conEnlaceUsado_debeLanzarEnlaceYaUsadoException() {
        EnlaceDescarga enlace = EnlaceDescarga.generar(UUID.randomUUID(), UUID.randomUUID(), "t0k3n");
        enlace.marcarComoUsado();
        
        assertThrows(EnlaceYaUsadoException.class, enlace::validar);
    }

    @Test
    void validar_conEnlaceExpirado_debeLanzarEnlaceExpiradoException() {
        EnlaceDescarga enlace = new EnlaceDescarga(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "t0k3n", LocalDateTime.now().minusMinutes(1), false);
        
        assertThrows(EnlaceExpiradoException.class, enlace::validar);
    }
}
