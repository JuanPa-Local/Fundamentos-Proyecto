package com.openlib.backend.infrastructure.cache;

import com.openlib.backend.domain.order.Carrito;
import com.openlib.backend.domain.order.CarritoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Reemplazado Redis por un mapa en memoria para que no falle al ejecutarse
 * en local sin Docker y sin Redis instalado.
 */
@Repository
public class RedisCarritoRepository implements CarritoRepository {

    // Usamos ConcurrentHashMap como almacenamiento en memoria simulando Redis
    private final ConcurrentHashMap<String, Carrito> inMemoryCache = new ConcurrentHashMap<>();
    private static final String KEY_PREFIX = "carrito:";

    @Override
    public void guardar(Carrito carrito, int ttlDias) {
        String key = KEY_PREFIX + carrito.getBuyerId().toString();
        inMemoryCache.put(key, carrito);
        // Nota: Al ser en memoria local, ignoramos el TTL ya que desaparecerá al apagar el backend.
    }

    @Override
    public Optional<Carrito> buscarPorBuyerId(UUID buyerId) {
        String key = KEY_PREFIX + buyerId.toString();
        Carrito carrito = inMemoryCache.get(key);
        return Optional.ofNullable(carrito);
    }

    @Override
    public void eliminar(UUID buyerId) {
        String key = KEY_PREFIX + buyerId.toString();
        inMemoryCache.remove(key);
    }
}
