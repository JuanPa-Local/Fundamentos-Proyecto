package com.openlib.backend.config;

import com.openlib.backend.domain.order.Carrito;
import com.openlib.backend.domain.order.SesionCheckout;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
@SuppressWarnings("deprecation")
public class RedisConfig {

    private final ObjectMapper objectMapper;

    public RedisConfig() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Bean
    public RedisTemplate<String, Carrito> redisCarritoTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Carrito> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer<>(objectMapper, Carrito.class));
        return template;
    }

    @Bean
    public RedisTemplate<String, SesionCheckout> redisSesionCheckoutTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, SesionCheckout> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer<>(objectMapper, SesionCheckout.class));
        return template;
    }
}
