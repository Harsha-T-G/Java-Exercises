package org.example.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ModelValidationTest {

    @Test
    void givenNullRequiredOrderFields_whenConstructingOrder_thenRejectsThem() {
        assertEquals("Order ID must not be null", assertThrows(NullPointerException.class,
                () -> new Order(null, CustomerType.REGULAR, BigDecimal.TEN, 1, Optional.empty())).getMessage());
        assertEquals("Customer type must not be null", assertThrows(NullPointerException.class,
                () -> new Order("1", null, BigDecimal.TEN, 1, Optional.empty())).getMessage());
        assertEquals("Amount must not be null", assertThrows(NullPointerException.class,
                () -> new Order("1", CustomerType.REGULAR, null, 1, Optional.empty())).getMessage());
    }

    @Test
    void givenInvalidDiscountResult_whenConstructing_thenRejectsBrokenInvariants() {
        assertEquals("Final amount cannot be negative", assertThrows(IllegalArgumentException.class,
                () -> new DiscountResult(BigDecimal.TEN, BigDecimal.TEN, BigDecimal.ONE.negate())).getMessage());
        assertEquals("Discount amount cannot exceed original amount", assertThrows(IllegalArgumentException.class,
                () -> new DiscountResult(BigDecimal.TEN, new BigDecimal("11"), BigDecimal.ZERO)).getMessage());
        assertEquals("Final amount must equal original amount minus discount amount",
                assertThrows(IllegalArgumentException.class,
                        () -> new DiscountResult(BigDecimal.TEN, BigDecimal.ONE, BigDecimal.TEN)).getMessage());
    }
}
