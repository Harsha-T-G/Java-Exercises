package org.example.pipeline;

import org.example.model.CustomerType;
import org.example.model.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class CorporateCustomerDiscountTest {

    private CorporateCustomerDiscount discount;

    @BeforeEach
    void setUp() {
        discount = new CorporateCustomerDiscount();
    }

    @Test
    void givenCorporateCustomer_withAmount100_whenCalculatingDiscount_thenReturns15Percent() {
        Order order = new Order(
                "123",
                CustomerType.CORPORATE,
                new BigDecimal("100"),
                1,
                Optional.empty()
        );

        BigDecimal discountAmount = discount.apply(order);
        // 15% of 100 = 15
        assertEquals(new BigDecimal("15.00"), discountAmount);
    }

    @Test
    void givenCorporateCustomer_withAmountZero_whenCalculatingDiscount_thenReturnsZero() {
        Order order = new Order(
                "123",
                CustomerType.CORPORATE,
                BigDecimal.ZERO,
                1,
                Optional.empty()
        );

        BigDecimal discountAmount = discount.apply(order);
        // When amount is 0 (with scale 2), 15% of 0 is 0 with same scale
        assertEquals(new BigDecimal("0.00"), discountAmount);
    }

    @Test
    void givenCorporateCustomer_withAmount199_99_whenCalculatingDiscount_thenReturnsCorrectPercentage() {
        Order order = new Order(
                "123",
                CustomerType.CORPORATE,
                new BigDecimal("199.99"),
                5,
                Optional.empty()
        );

        BigDecimal discountAmount = discount.apply(order);
        // 15% of 199.99 = 29.9985 (4 decimal places due to multiplication)
        assertEquals(new BigDecimal("29.9985"), discountAmount);
    }
}