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
        template.setValueSerializer(new CustomJsonRedisSerializer<>(this.objectMapper, Carrito.class));
        return template;
    }

    @Bean
    public RedisTemplate<String, SesionCheckout> redisSesionCheckoutTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, SesionCheckout> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new CustomJsonRedisSerializer<>(this.objectMapper, SesionCheckout.class));
        return template;
    }

    // Serializador personalizado para evitar las advertencias de deprecación de Spring Data Redis 4.0
    static class CustomJsonRedisSerializer<T> implements org.springframework.data.redis.serializer.RedisSerializer<T> {
        private final ObjectMapper mapper;
        private final Class<T> type;

        public CustomJsonRedisSerializer(ObjectMapper mapper, Class<T> type) {
            this.mapper = mapper;
            this.type = type;
        }

        @Override
        public byte[] serialize(T t) throws org.springframework.data.redis.serializer.SerializationException {
            if (t == null) return new byte[0];
            try {
                return mapper.writeValueAsBytes(t);
            } catch (Exception e) {
                throw new org.springframework.data.redis.serializer.SerializationException("Error serializing", e);
            }
        }

        @Override
        public T deserialize(byte[] bytes) throws org.springframework.data.redis.serializer.SerializationException {
            if (bytes == null || bytes.length == 0) return null;
            try {
                return mapper.readValue(bytes, type);
            } catch (Exception e) {
                throw new org.springframework.data.redis.serializer.SerializationException("Error deserializing", e);
            }
        }
    }
}
