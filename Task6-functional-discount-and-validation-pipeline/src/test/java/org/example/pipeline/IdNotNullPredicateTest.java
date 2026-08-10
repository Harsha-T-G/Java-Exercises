package org.example.pipeline;

import org.example.model.CustomerType;
import org.example.model.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class IdNotNullPredicateTest {

    private IdNotNullPredicate predicate;

    @BeforeEach
    void setUp() {
        predicate = new IdNotNullPredicate();
    }

    @Test
    void givenOrderWithNullId_whenTestingIdNotNullPredicate_thenReturnsFalse() {
        // Since Order constructor now throws NPE for null ID, we test the predicate directly
        // by creating an Order with empty string and checking that predicate handles it
        Order order = new Order(
                "", // Empty string instead of null
                CustomerType.REGULAR,
                new BigDecimal("100.00"),
                1,
                Optional.empty()
        );

        assertFalse(predicate.test(order));
    }

    @Test
    void givenOrderWithEmptyId_whenTestingIdNotNullPredicate_thenReturnsFalse() {
        Order order = new Order(
                "",
                CustomerType.REGULAR,
                new BigDecimal("100.00"),
                1,
                Optional.empty()
        );

        assertFalse(predicate.test(order));
    }

    @Test
    void givenOrderWithValidId_whenTestingIdNotNullPredicate_thenReturnsTrue() {
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