package com.openlib.backend.domain.order;

import com.openlib.backend.application.usecase.VerBibliotecaUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/library")
public class BibliotecaRestController {

    private final VerBibliotecaUseCase verBibliotecaUseCase;

    public BibliotecaRestController(VerBibliotecaUseCase verBibliotecaUseCase) {
        this.verBibliotecaUseCase = verBibliotecaUseCase;
    }

    @GetMapping("/{buyerId}")
    public ResponseEntity<List<LibroBiblioteca>> getLibrary(@PathVariable UUID buyerId) {
        return ResponseEntity.ok(verBibliotecaUseCase.ejecutar(buyerId));
    }
}
