package org.example.pipeline;

import org.example.model.Order;

import java.util.function.Predicate;

public class PositiveItemCountPredicate implements Predicate<Order> {
    @Override
    public boolean test(Order order) {
        return order.itemCount() > 0;
    }
}