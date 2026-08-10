package org.example.pipeline;

import org.example.model.Order;

import java.util.function.Predicate;

public class IdNotNullPredicate implements Predicate<Order> {
    @Override
    public boolean test(Order order) {
        return order.id() != null && !order.id().isEmpty();
    }
}