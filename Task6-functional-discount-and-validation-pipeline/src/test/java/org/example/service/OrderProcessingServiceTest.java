package org.example.service;

import org.example.model.CustomerType;
import org.example.model.DiscountResult;
import org.example.model.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class OrderProcessingServiceTest {

    private OrderProcessingService service;

    @BeforeEach
    void setUp() {
        service = new OrderProcessingService();
    }

    @Test
    void givenRegularCustomer_withAmount100_andNoCoupon_whenProcessingOrder_thenReturnsCorrectDiscount() {
        Order order = new Order(
                "123",
                CustomerType.REGULAR,
                new BigDecimal("100.00"),
                2,
                Optional.empty()
        );

        DiscountResult result = service.processOrder(order);

        assertEquals(new BigDecimal("100.00"), result.originalAmount());
        assertEquals(new BigDecimal("5.00"), result.discountAmount()); // 5% of 100
        assertEquals(new BigDecimal("95.00"), result.finalAmount()); // 100 - 5
    }

    @Test
    void givenRegularCustomer_withAmount100_andCouponSave10_whenProcessingOrder_thenReturnsCorrectDiscount() {
        Order order = new Order(
                "124",
                CustomerType.REGULAR,
                new BigDecimal("100.00"),
                1,
                Optional.of("SAVE10")
        );

        DiscountResult result = service.processOrder(order);

        assertEquals(new BigDecimal("100.00"), result.originalAmount());
        assertEquals(new BigDecimal("14.5000"), result.discountAmount());
        assertEquals(new BigDecimal("85.5000"), result.finalAmount()); // 100 - 14.5
    }

    @Test
    void givenPremiumCustomer_withAmount200_andNoCoupon_whenProcessingOrder_thenReturnsCorrectDiscount() {
        Order order = new Order(
                "125",
                CustomerType.PREMIUM,
                new BigDecimal("200.00"),
                3,
                Optional.empty()
        );

        DiscountResult result = service.processOrder(order);

        assertEquals(new BigDecimal("200.00"), result.originalAmount());
        assertEquals(new BigDecimal("20.00"), result.discountAmount()); // 10% of 200
        assertEquals(new BigDecimal("180.00"), result.finalAmount()); // 200 - 20
    }

    @Test
    void givenCorporateCustomer_withAmount150_andCouponSave20_whenProcessingOrder_thenReturnsCorrectDiscount() {
        Order order = new Order(
                "126",
                CustomerType.CORPORATE,
                new BigDecimal("150.00"),
                1,
                Optional.of("SAVE20")
        );

        DiscountResult result = service.processOrder(order);

        assertEquals(new BigDecimal("150.00"), result.originalAmount());
        assertEquals(new BigDecimal("35.2500"), result.discountAmount());
        assertEquals(new BigDecimal("114.7500"), result.finalAmount()); // 150 - 35.25
    }

    @Test
    void givenInvalidOrder_withEmptyId_whenProcessingOrder_thenThrowsIllegalStateException() {
        Order order = new Order(
                "", // Invalid ID
                CustomerType.REGULAR,
                new BigDecimal("100.00"),
                2,
                Optional.empty()
        );

        assertThrows(IllegalStateException.class, () ->
                service.processOrder(order)
        );
    }

    @Test
    void givenRegularCustomer_withAmount50_andLargeCoupon_whenProcessingOrder_thenReturnsCorrectDiscount() {
        Order order = new Order(
                "127",
                CustomerType.REGULAR,
                new BigDecimal("50.00"),
                1,
                Optional.of("BIGSAVE")
        );

        DiscountResult result = service.processOrder(order);

        assertEquals(new BigDecimal("50.00"), result.originalAmount());
        assertEquals(new BigDecimal("7.2500"), result.discountAmount());
        assertEquals(new BigDecimal("42.7500"), result.finalAmount());
    }

    @Test
    void givenPremiumCustomer_withAmount80_andNoCoupon_whenProcessingOrderUsingOverloadedMethod_thenReturnsCorrectDiscount() {
        Order order = new Order(
                "128",
                CustomerType.PREMIUM,
                new BigDecimal("80.00"),
                2,
                Optional.empty()
        );

        // Test the overloaded method that doesn't take coupon
        DiscountResult result = service.processOrder(order);

        assertEquals(new BigDecimal("80.00"), result.originalAmount());
        assertEquals(new BigDecimal("8.00"), result.discountAmount()); // 10% of 80
        assertEquals(new BigDecimal("72.00"), result.finalAmount()); // 80 - 8
    }
}