package org.example.pipeline;

import org.example.model.CustomerType;
import org.example.model.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class PremiumCustomerDiscountTest {

    private PremiumCustomerDiscount discount;

    @BeforeEach
    void setUp() {
        discount = new PremiumCustomerDiscount();
    }

    @Test
    void givenPremiumCustomer_withAmount100_whenCalculatingDiscount_thenReturns10Percent() {
        Order order = new Order(
                "123",
                CustomerType.PREMIUM,
                new BigDecimal("100"),
                1,
                Optional.empty()
        );

        BigDecimal discountAmount = discount.apply(order);
        // 10% of 100 = 10
        assertEquals(new BigDecimal("10.00"), discountAmount);
    }

    @Test
    void givenPremiumCustomer_withAmountZero_whenCalculatingDiscount_thenReturnsZero() {
        Order order = new Order(
                "123",
                CustomerType.PREMIUM,
                BigDecimal.ZERO,
                1,
                Optional.empty()
        );

        BigDecimal discountAmount = discount.apply(order);
        // When amount is 0 (with scale 2), 10% of 0 is 0 with same scale
        assertEquals(new BigDecimal("0.00"), discountAmount);
    }

    @Test
    void givenPremiumCustomer_withAmount199_99_whenCalculatingDiscount_thenReturnsCorrectPercentage() {
        Order order = new Order(
                "123",
                CustomerType.PREMIUM,
                new BigDecimal("199.99"),
                5,
                Optional.empty()
        );

        BigDecimal discountAmount = discount.apply(order);
        // 10% of 199.99 = 19.9990 (4 decimal places due to multiplication)
        assertEquals(new BigDecimal("19.9990"), discountAmount);
    }
}