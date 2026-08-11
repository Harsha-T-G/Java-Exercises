package org.example.pipeline;

import org.example.model.Order;

import java.math.BigDecimal;
import java.util.function.Function;

public class RegularCustomerDiscount implements Function<Order, BigDecimal> {
    private static final PercentageCustomerDiscount DELEGATE =
            new PercentageCustomerDiscount(new BigDecimal("0.05"));

    @Override
    public BigDecimal apply(Order order) {
        return DELEGATE.apply(order);
    }
}
