package com.openlib.backend.application.service;

import com.openlib.backend.domain.order.*;
import com.openlib.backend.domain.order.exception.EnlaceExpiradoException;
import com.openlib.backend.domain.order.exception.LibroNoEnBibliotecaException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class DescargaFacade {

    private final EnlaceDescargaRepository enlaceDescargaRepository;
    private final BibliotecaRepository bibliotecaRepository;
    private final TokenGateway tokenGateway;

    public DescargaFacade(EnlaceDescargaRepository enlaceDescargaRepository,
                          BibliotecaRepository bibliotecaRepository,
                          TokenGateway tokenGateway) {
        this.enlaceDescargaRepository = enlaceDescargaRepository;
        this.bibliotecaRepository = bibliotecaRepository;
        this.tokenGateway = tokenGateway;
    }

    @Transactional
    public EnlaceDescarga generarEnlace(UUID buyerId, UUID libroId) {
        if (!bibliotecaRepository.existeEnBiblioteca(buyerId, libroId)) {
            throw new LibroNoEnBibliotecaException("No puedes descargar un libro que no está en tu biblioteca.");
        }

        String token = tokenGateway.generar();
        EnlaceDescarga enlace = EnlaceDescarga.generar(buyerId, libroId, token);
        
        return enlaceDescargaRepository.guardar(enlace);
    }

    @Transactional
    public String ejecutarDescarga(String token, String ipOrigen) {
        EnlaceDescarga enlace = enlaceDescargaRepository.buscarPorToken(token)
                .orElseThrow(() -> new EnlaceExpiradoException("El enlace de descarga es inválido o no existe."));

        enlace.validar();
        enlace.marcarComoUsado();
        
        enlaceDescargaRepository.guardar(enlace);

        RegistroDescarga registro = new RegistroDescarga(
                enlace.getBuyerId(),
                enlace.getLibroId(),
                ipOrigen,
                LocalDateTime.now()
        );
        enlaceDescargaRepository.guardarRegistro(registro);

        // Simulamos la devolución del archivo / URL (usualmente se delegaría a un ArchivoGateway)
        return "https://openlib.market/downloads/" + enlace.getLibroId() + "/file.pdf";
    }
}
