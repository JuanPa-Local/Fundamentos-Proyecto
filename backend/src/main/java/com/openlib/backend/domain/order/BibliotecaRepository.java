package com.openlib.backend.domain.order;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BibliotecaRepository {
    boolean existeEnBiblioteca(UUID buyerId, UUID libroId);
    void agregar(UUID buyerId, UUID libroId);
    List<LibroBiblioteca> buscarPorBuyerId(UUID buyerId);
}
