package com.openlib.backend.domain.order.strategy;

import org.springframework.stereotype.Component;

@Component
public class VipDiscount implements DiscountStrategy {
    @Override
    public double calculate(double basePrice) {
        return basePrice * 0.8; // 20% discount
    }
}
