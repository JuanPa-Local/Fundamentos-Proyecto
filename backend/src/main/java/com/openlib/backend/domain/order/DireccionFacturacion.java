package com.openlib.backend.domain.order;

import com.openlib.backend.domain.order.exception.CampoDireccionObligatorioException;
import lombok.Getter;

@Getter
public class DireccionFacturacion {
    private final String calle;
    private final String ciudad;
    private final String departamento;
    private final String pais;

    public DireccionFacturacion(String calle, String ciudad, String departamento, String pais) {
        if (calle == null || calle.trim().isEmpty()) {
            throw new CampoDireccionObligatorioException("La calle es obligatoria.");
        }
        if (ciudad == null || ciudad.trim().isEmpty()) {
            throw new CampoDireccionObligatorioException("La ciudad es obligatoria.");
        }
        if (departamento == null || departamento.trim().isEmpty()) {
            throw new CampoDireccionObligatorioException("El departamento es obligatorio.");
        }
        if (pais == null || pais.trim().isEmpty()) {
            throw new CampoDireccionObligatorioException("El país es obligatorio.");
        }
        this.calle = calle;
        this.ciudad = ciudad;
        this.departamento = departamento;
        this.pais = pais;
    }
}
