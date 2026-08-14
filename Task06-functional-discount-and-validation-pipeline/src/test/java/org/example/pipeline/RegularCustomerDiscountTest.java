package org.example.pipeline;

import org.example.model.CustomerType;
import org.example.model.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class RegularCustomerDiscountTest {

    private RegularCustomerDiscount discount;

    @BeforeEach
    void setUp() {
        discount = new RegularCustomerDiscount();
    }

    @Test
    void givenRegularCustomer_withAmount100_whenCalculatingDiscount_thenReturns5Percent() {
        Order order = new Order(
                "123",
                CustomerType.REGULAR,
                new BigDecimal("100"),
                1,
                Optional.empty()
        );

        BigDecimal discountAmount = discount.apply(order);
        // 5% of 100 = 5
        assertEquals(new BigDecimal("5.00"), discountAmount);
    }

    @Test
    void givenRegularCustomer_withAmountZero_whenCalculatingDiscount_thenReturnsZero() {
        Order order = new Order(
                "123",
                CustomerType.REGULAR,
                BigDecimal.ZERO,
                1,
                Optional.empty()
        );

        BigDecimal discountAmount = discount.apply(order);
        // When amount is 0 (with scale 2), 5% of 0 is 0 with same scale
        assertEquals(new BigDecimal("0.00"), discountAmount);
    }

    @Test
    void givenRegularCustomer_withAmount199_99_whenCalculatingDiscount_thenReturnsCorrectPercentage() {
        Order order = new Order(
                "123",
                CustomerType.REGULAR,
                new BigDecimal("199.99"),
                5,
                Optional.empty()
        );

        BigDecimal discountAmount = discount.apply(order);
        // 5% of 199.99 = 9.9995 (4 decimal places due to multiplication)
        assertEquals(new BigDecimal("9.9995"), discountAmount);
    }
}