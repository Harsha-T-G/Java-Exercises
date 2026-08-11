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
        assertEquals(new BigDecimal("14.50"), result.discountAmount());
        assertEquals(new BigDecimal("85.50"), result.finalAmount()); // 100 - 14.5
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
        assertEquals(new BigDecimal("48.00"), result.discountAmount());
        assertEquals(new BigDecimal("102.00"), result.finalAmount()); // 15% tier, then 20% coupon
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
    void givenUnknownCoupon_whenProcessingOrder_thenRejectsCoupon() {
        Order order = new Order(
                "127",
                CustomerType.REGULAR,
                new BigDecimal("50.00"),
                1,
                Optional.of("BIGSAVE")
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class, () -> service.processOrder(order));
        assertEquals("Unknown coupon code: BIGSAVE", exception.getMessage());
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

    @Test
    void givenCorporateCustomer_withFree20Coupon_whenProcessingOrder_thenAppliesTwentyDollarCoupon() {
        Order order = new Order("129", CustomerType.CORPORATE, new BigDecimal("150.00"), 2,
                Optional.of("FREE20"));

        DiscountResult result = service.processOrder(order);

        assertEquals(new BigDecimal("42.50"), result.discountAmount());
        assertEquals(new BigDecimal("107.50"), result.finalAmount());
    }

    @Test
    void givenNullOrder_whenProcessingOrder_thenFailsAtServiceBoundary() {
        NullPointerException exception = assertThrows(
                NullPointerException.class, () -> service.processOrder(null));
        assertEquals("Order cannot be null", exception.getMessage());
    }

    @Test
    void givenNullCouponOptional_whenUsingExplicitOverload_thenFailsAtServiceBoundary() {
        Order order = new Order("130", CustomerType.REGULAR, new BigDecimal("20.00"), 1,
                Optional.empty());

        NullPointerException exception = assertThrows(
                NullPointerException.class, () -> service.processOrder(order, null));
        assertEquals("Coupon code cannot be null", exception.getMessage());
    }
}
