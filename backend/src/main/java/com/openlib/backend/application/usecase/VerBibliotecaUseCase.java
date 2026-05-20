package com.openlib.backend.application.usecase;

import com.openlib.backend.domain.order.BibliotecaRepository;
import com.openlib.backend.domain.order.LibroBiblioteca;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class VerBibliotecaUseCase {

    private final BibliotecaRepository bibliotecaRepository;

    public VerBibliotecaUseCase(BibliotecaRepository bibliotecaRepository) {
        this.bibliotecaRepository = bibliotecaRepository;
    }

    public List<LibroBiblioteca> ejecutar(UUID buyerId) {
        return bibliotecaRepository.buscarPorBuyerId(buyerId);
    }
}
