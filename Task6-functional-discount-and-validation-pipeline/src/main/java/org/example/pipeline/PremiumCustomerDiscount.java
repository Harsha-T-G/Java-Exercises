package org.example.pipeline;

import org.example.model.Order;

import java.math.BigDecimal;
import java.util.function.Function;

public class PremiumCustomerDiscount implements Function<Order, BigDecimal> {
    @Override
    public BigDecimal apply(Order order) {
        // Premium customers get 10% discount
        return order.amount().multiply(new BigDecimal("0.10"));
    }
}