package org.example.pipeline;

import org.example.model.Order;

import java.math.BigDecimal;
import java.util.function.Predicate;

public class PositiveAmountPredicate implements Predicate<Order> {
    @Override
    public boolean test(Order order) {
        return order.amount().compareTo(BigDecimal.ZERO) > 0;
    }
}