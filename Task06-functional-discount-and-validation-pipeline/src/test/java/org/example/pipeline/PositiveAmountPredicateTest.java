package org.example.pipeline;

import org.example.model.CustomerType;
import org.example.model.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class PositiveAmountPredicateTest {

    private PositiveAmountPredicate predicate;

    @BeforeEach
    void setUp() {
        predicate = new PositiveAmountPredicate();
    }

    @Test
    void givenOrderWithZeroAmount_whenTestingPositiveAmountPredicate_thenReturnsFalse() {
        Order order = new Order(
                "123",
                CustomerType.REGULAR,
                BigDecimal.ZERO,
                1,
                Optional.empty()
        );

        assertFalse(predicate.test(order));
    }

    @Test
    void givenOrderWithNegativeAmount_whenTestingPositiveAmountPredicate_thenReturnsFalse() {
        Order order = new Order(
                "123",
                CustomerType.REGULAR,
                new BigDecimal("-10.00"),
                1,
                Optional.empty()
        );

        assertFalse(predicate.test(order));
    }

    @Test
    void givenOrderWithPositiveAmount_whenTestingPositiveAmountPredicate_thenReturnsTrue() {
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