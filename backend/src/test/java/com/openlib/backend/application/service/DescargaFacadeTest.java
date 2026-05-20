package com.openlib.backend.application.service;

import com.openlib.backend.domain.order.*;
import com.openlib.backend.domain.order.exception.EnlaceExpiradoException;
import com.openlib.backend.domain.order.exception.EnlaceYaUsadoException;
import com.openlib.backend.domain.order.exception.LibroNoEnBibliotecaException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DescargaFacadeTest {

    private EnlaceDescargaRepository enlaceDescargaRepository;
    private BibliotecaRepository bibliotecaRepository;
    private TokenGateway tokenGateway;
    private DescargaFacade descargaFacade;

    @BeforeEach
    void setUp() {
        enlaceDescargaRepository = mock(EnlaceDescargaRepository.class);
        bibliotecaRepository = mock(BibliotecaRepository.class);
        tokenGateway = mock(TokenGateway.class);
        descargaFacade = new DescargaFacade(enlaceDescargaRepository, bibliotecaRepository, tokenGateway);
    }

    @Test
    void generarEnlace_conLibroEnBiblioteca_debeGenerarYGuardar() {
        UUID buyerId = UUID.randomUUID();
        UUID libroId = UUID.randomUUID();
        String token = "abc-123";

        when(bibliotecaRepository.existeEnBiblioteca(buyerId, libroId)).thenReturn(true);
        when(tokenGateway.generar()).thenReturn(token);
        when(enlaceDescargaRepository.guardar(any())).thenAnswer(invocation -> invocation.getArgument(0));

        EnlaceDescarga enlace = descargaFacade.generarEnlace(buyerId, libroId);

        assertNotNull(enlace);
        assertEquals(token, enlace.getToken());
        verify(enlaceDescargaRepository).guardar(any(EnlaceDescarga.class));
    }

    @Test
    void generarEnlace_conLibroFueraDeBiblioteca_debeLanzarExcepcion() {
        UUID buyerId = UUID.randomUUID();
        UUID libroId = UUID.randomUUID();

        when(bibliotecaRepository.existeEnBiblioteca(buyerId, libroId)).thenReturn(false);

        assertThrows(LibroNoEnBibliotecaException.class, () -> descargaFacade.generarEnlace(buyerId, libroId));
        verify(enlaceDescargaRepository, never()).guardar(any());
    }

    @Test
    void ejecutarDescarga_conTokenValido_debeMarcarUsadoYRegistrar() {
        String token = "abc-123";
        EnlaceDescarga enlace = EnlaceDescarga.generar(UUID.randomUUID(), UUID.randomUUID(), token);

        when(enlaceDescargaRepository.buscarPorToken(token)).thenReturn(Optional.of(enlace));

        String url = descargaFacade.ejecutarDescarga(token, "192.168.1.1");

        assertTrue(url.contains(enlace.getLibroId().toString()));
        assertTrue(enlace.isUsado());
        
        verify(enlaceDescargaRepository).guardar(enlace);
        
        ArgumentCaptor<RegistroDescarga> captor = ArgumentCaptor.forClass(RegistroDescarga.class);
        verify(enlaceDescargaRepository).guardarRegistro(captor.capture());
        
        RegistroDescarga registro = captor.getValue();
        assertEquals("192.168.1.1", registro.getIpOrigen());
        assertEquals(enlace.getBuyerId(), registro.getBuyerId());
    }

    @Test
    void ejecutarDescarga_conTokenYaUsado_debeLanzarExcepcion() {
        String token = "abc-123";
        EnlaceDescarga enlace = EnlaceDescarga.generar(UUID.randomUUID(), UUID.randomUUID(), token);
        enlace.marcarComoUsado();

        when(enlaceDescargaRepository.buscarPorToken(token)).thenReturn(Optional.of(enlace));

        assertThrows(EnlaceYaUsadoException.class, () -> descargaFacade.ejecutarDescarga(token, "192.168.1.1"));
        verify(enlaceDescargaRepository, never()).guardarRegistro(any());
    }
}
