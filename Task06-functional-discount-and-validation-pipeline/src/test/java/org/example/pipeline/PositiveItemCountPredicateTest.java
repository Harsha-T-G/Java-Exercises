package org.example.pipeline;

import org.example.model.CustomerType;
import org.example.model.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class PositiveItemCountPredicateTest {

    private PositiveItemCountPredicate predicate;

    @BeforeEach
    void setUp() {
        predicate = new PositiveItemCountPredicate();
    }

    @Test
    void givenOrderWithZeroItemCount_whenTestingPositiveItemCountPredicate_thenReturnsFalse() {
        Order order = new Order(
                "123",
                CustomerType.REGULAR,
                new BigDecimal("100.00"),
                0,
                Optional.empty()
        );

        assertFalse(predicate.test(order));
    }

    @Test
    void givenOrderWithNegativeItemCount_whenTestingPositiveItemCountPredicate_thenReturnsFalse() {
        Order order = new Order(
                "123",
                CustomerType.REGULAR,
                new BigDecimal("100.00"),
                -1,
                Optional.empty()
        );

        assertFalse(predicate.test(order));
    }

    @Test
    void givenOrderWithPositiveItemCount_whenTestingPositiveItemCountPredicate_thenReturnsTrue() {
        Order order = new Order(
                "123",
                CustomerType.REGULAR,
                new BigDecimal("100.00"),
                1,
                Optional.empty()
        );

        assertTrue(predicate.test(order));
    }
}