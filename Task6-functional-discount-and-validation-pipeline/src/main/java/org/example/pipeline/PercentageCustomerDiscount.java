package org.example.pipeline;

import org.example.model.Order;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.function.Function;

public final class PercentageCustomerDiscount implements Function<Order, BigDecimal> {
    private final BigDecimal rate;

    public PercentageCustomerDiscount(BigDecimal rate) {
        this.rate = Objects.requireNonNull(rate, "Discount rate cannot be null");
        if (rate.compareTo(BigDecimal.ZERO) < 0 || rate.compareTo(BigDecimal.ONE) > 0) {
            throw new IllegalArgumentException("Discount rate must be between 0 and 1");
        }
    }

    @Override
    public BigDecimal apply(Order order) {
        Objects.requireNonNull(order, "Order cannot be null");
        return order.amount().multiply(rate);
    }
}
