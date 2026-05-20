package com.openlib.backend.domain.order.strategy;

import org.springframework.stereotype.Component;

@Component
public class SeasonalDiscount implements DiscountStrategy {
    @Override
    public double calculate(double basePrice) {
        return basePrice * 0.9; // 10% discount
    }
}
