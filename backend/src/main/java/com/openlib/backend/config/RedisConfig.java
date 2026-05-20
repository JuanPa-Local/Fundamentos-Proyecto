package com.openlib.backend.config;

import com.openlib.backend.domain.order.Carrito;
import com.openlib.backend.domain.order.SesionCheckout;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Carrito> redisCarritoTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Carrito> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new Jackson2JsonRedisSerializer<>(Carrito.class));
        return template;
    }

    @Bean
    public RedisTemplate<String, SesionCheckout> redisSesionCheckoutTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, SesionCheckout> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new Jackson2JsonRedisSerializer<>(SesionCheckout.class));
        return template;
    }
}
