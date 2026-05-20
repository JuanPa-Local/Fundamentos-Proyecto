package com.openlib.backend.domain.order.strategy;

public interface DiscountStrategy {
    double calculate(double basePrice);
}
