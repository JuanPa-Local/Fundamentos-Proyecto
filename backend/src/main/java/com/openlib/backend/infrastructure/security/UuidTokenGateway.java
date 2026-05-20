package com.openlib.backend.infrastructure.security;

import com.openlib.backend.domain.order.TokenGateway;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UuidTokenGateway implements TokenGateway {
    @Override
    public String generar() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
