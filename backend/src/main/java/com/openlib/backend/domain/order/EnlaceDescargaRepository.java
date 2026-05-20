package com.openlib.backend.domain.order;

import java.util.Optional;

public interface EnlaceDescargaRepository {
    EnlaceDescarga guardar(EnlaceDescarga enlace);
    Optional<EnlaceDescarga> buscarPorToken(String token);
    void guardarRegistro(RegistroDescarga registro);
}
