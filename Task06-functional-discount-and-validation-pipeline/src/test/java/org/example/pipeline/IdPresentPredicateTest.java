package org.example.pipeline;

import org.example.model.CustomerType;
import org.example.model.Order;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IdPresentPredicateTest {
    private final IdPresentPredicate predicate = new IdPresentPredicate();

    @Test
    void givenPresentId_whenTesting_thenReturnsTrue() {
        assertTrue(predicate.test(orderWithId("ORD-1")));
    }

    @Test
    void givenEmptyOrBlankId_whenTesting_thenReturnsFalse() {
        assertFalse(predicate.test(orderWithId("")));
        assertFalse(predicate.test(orderWithId("   ")));
    }

    @Test
    void givenNullOrder_whenTesting_thenReturnsFalse() {
        assertFalse(predicate.test(null));
    }

    private static Order orderWithId(String id) {
        return new Order(id, CustomerType.REGULAR, BigDecimal.TEN, 1, Optional.empty());
    }
}
