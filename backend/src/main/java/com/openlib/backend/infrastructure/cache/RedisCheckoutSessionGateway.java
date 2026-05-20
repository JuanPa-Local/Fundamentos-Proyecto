package com.openlib.backend.infrastructure.cache;

import com.openlib.backend.domain.order.CheckoutSessionGateway;
import com.openlib.backend.domain.order.SesionCheckout;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Repository
public class RedisCheckoutSessionGateway implements CheckoutSessionGateway {

    private final RedisTemplate<String, SesionCheckout> redisTemplate;
    private static final String KEY_PREFIX = "checkout:";

    public RedisCheckoutSessionGateway(RedisTemplate<String, SesionCheckout> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void guardar(SesionCheckout sesion, int ttlMinutos) {
        String key = KEY_PREFIX + sesion.getBuyerId().toString();
        redisTemplate.opsForValue().set(key, sesion);
        redisTemplate.expire(key, ttlMinutos, TimeUnit.MINUTES);
    }

    @Override
    public Optional<SesionCheckout> buscarPorBuyerId(UUID buyerId) {
        String key = KEY_PREFIX + buyerId.toString();
        SesionCheckout sesion = redisTemplate.opsForValue().get(key);
        return Optional.ofNullable(sesion);
    }

    @Override
    public void eliminar(UUID buyerId) {
        String key = KEY_PREFIX + buyerId.toString();
        redisTemplate.delete(key);
    }
}
