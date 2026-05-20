package com.openlib.backend.infrastructure.persistence;

import com.openlib.backend.domain.order.EnlaceDescarga;
import com.openlib.backend.domain.order.EnlaceDescargaRepository;
import com.openlib.backend.domain.order.RegistroDescarga;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class EnlaceDescargaRepositoryAdapter implements EnlaceDescargaRepository {

    private final SpringEnlaceDescargaRepository springEnlaceDescargaRepository;
    private final SpringRegistroDescargaRepository springRegistroDescargaRepository;

    public EnlaceDescargaRepositoryAdapter(SpringEnlaceDescargaRepository springEnlaceDescargaRepository,
                                           SpringRegistroDescargaRepository springRegistroDescargaRepository) {
        this.springEnlaceDescargaRepository = springEnlaceDescargaRepository;
        this.springRegistroDescargaRepository = springRegistroDescargaRepository;
    }

    @Override
    public EnlaceDescarga guardar(EnlaceDescarga enlace) {
        EnlaceDescargaJpaEntity entity = new EnlaceDescargaJpaEntity();
        entity.setId(enlace.getId());
        entity.setBuyerId(enlace.getBuyerId());
        entity.setLibroId(enlace.getLibroId());
        entity.setToken(enlace.getToken());
        entity.setExpiracion(enlace.getExpiracion());
        entity.setUsado(enlace.isUsado());

        springEnlaceDescargaRepository.save(entity);
        return enlace;
    }

    @Override
    public Optional<EnlaceDescarga> buscarPorToken(String token) {
        return springEnlaceDescargaRepository.findByToken(token)
                .map(entity -> new EnlaceDescarga(
                        entity.getId(),
                        entity.getBuyerId(),
                        entity.getLibroId(),
                        entity.getToken(),
                        entity.getExpiracion(),
                        entity.isUsado()
                ));
    }

    @Override
    public void guardarRegistro(RegistroDescarga registro) {
        RegistroDescargaJpaEntity entity = new RegistroDescargaJpaEntity();
        entity.setId(java.util.UUID.randomUUID());
        entity.setBuyerId(registro.getBuyerId());
        entity.setLibroId(registro.getLibroId());
        entity.setIpOrigen(registro.getIpOrigen());
        entity.setTimestamp(registro.getTimestamp());

        springRegistroDescargaRepository.save(entity);
    }
}
