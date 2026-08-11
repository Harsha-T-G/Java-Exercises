package org.example.pipeline;

import org.example.model.Order;

import java.util.function.Predicate;

public final class IdPresentPredicate implements Predicate<Order> {
    @Override
    public boolean test(Order order) {
        return order != null && order.id() != null && !order.id().isBlank();
    }
}
