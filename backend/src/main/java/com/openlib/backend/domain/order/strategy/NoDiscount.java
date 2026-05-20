package com.openlib.backend.domain.order.strategy;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class NoDiscount implements DiscountStrategy {
    @Override
    public double calculate(double basePrice) {
        return basePrice;
    }
}
