package com.openlib.backend.application.usecase;

import com.openlib.backend.domain.order.DireccionFacturacion;
import com.openlib.backend.domain.user.User;
import com.openlib.backend.domain.user.UserRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ObtenerDireccionPrerrellenaUseCase {

    private final UserRepository userRepository;

    public ObtenerDireccionPrerrellenaUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public DireccionFacturacion ejecutar(UUID buyerId) {
        // En una implementación completa US-007 (PerfilUsuario), se extraería la dirección real del perfil.
        // Aquí simulamos que no hay dirección prerrellenada hasta que el perfil esté completo.
        return null; 
    }
}
