package org.example.pipeline;

import org.example.model.Order;

import java.math.BigDecimal;
import java.util.function.Function;

public class RegularCustomerDiscount implements Function<Order, BigDecimal> {
    @Override
    public BigDecimal apply(Order order) {
        // Regular customers get 5% discount
        return order.amount().multiply(new BigDecimal("0.05"));
    }
}