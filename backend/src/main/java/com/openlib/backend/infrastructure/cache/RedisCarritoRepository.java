package com.openlib.backend.infrastructure.cache;

import com.openlib.backend.domain.order.Carrito;
import com.openlib.backend.domain.order.CarritoRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Repository
public class RedisCarritoRepository implements CarritoRepository {

    private final RedisTemplate<String, Carrito> redisTemplate;
    private static final String KEY_PREFIX = "carrito:";

    public RedisCarritoRepository(RedisTemplate<String, Carrito> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void guardar(Carrito carrito, int ttlDias) {
        String key = KEY_PREFIX + carrito.getBuyerId().toString();
        redisTemplate.opsForValue().set(key, carrito);
        redisTemplate.expire(key, ttlDias, TimeUnit.DAYS);
    }

    @Override
    public Optional<Carrito> buscarPorBuyerId(UUID buyerId) {
        String key = KEY_PREFIX + buyerId.toString();
        Carrito carrito = redisTemplate.opsForValue().get(key);
        return Optional.ofNullable(carrito);
    }

    @Override
    public void eliminar(UUID buyerId) {
        String key = KEY_PREFIX + buyerId.toString();
        redisTemplate.delete(key);
    }
}
