package org.example.pipeline;

import org.example.model.CustomerType;
import org.example.model.DiscountResult;
import org.example.model.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;

class DiscountPipelineTest {

    private DiscountPipeline pipeline;
    private RegularCustomerDiscount regularDiscount;
    private PremiumCustomerDiscount premiumDiscount;
    private CorporateCustomerDiscount corporateDiscount;

    @BeforeEach
    void setUp() {
        pipeline = new DiscountPipeline();
        regularDiscount = new RegularCustomerDiscount();
        premiumDiscount = new PremiumCustomerDiscount();
        corporateDiscount = new CorporateCustomerDiscount();
    }

    @Test
    void givenRegularCustomer_withAmount100_andNoCoupon_whenProcessingOrder_thenReturnsCorrectDiscount() {
        // Arrange
        Order order = new Order(
                "123",
                CustomerType.REGULAR,
                new BigDecimal("100.00"),
                2,
                Optional.empty()
        );

        List<Predicate<Order>> validationPredicates = getPredicates();

        // Act
        DiscountResult result = pipeline.processOrder(
                order,
                regularDiscount,
                premiumDiscount,
                corporateDiscount,
                validationPredicates,
                Optional.empty()
        );

        // Assert
        assertEquals(new BigDecimal("100.00"), result.originalAmount());
        assertEquals(new BigDecimal("5.00"), result.discountAmount()); // 5% of 100
        assertEquals(new BigDecimal("95.00"), result.finalAmount()); // 100 - 5
    }

    private static List<Predicate<Order>> getPredicates() {
        List<IdNotNullPredicate> idPredicates = List.of(new IdNotNullPredicate());
        List<PositiveAmountPredicate> amountPredicates = List.of(new PositiveAmountPredicate());
        List<PositiveItemCountPredicate> itemPredicates = List.of(new PositiveItemCountPredicate());

        List<Predicate<Order>> validationPredicates = new ArrayList<>();
        validationPredicates.addAll(idPredicates);
        validationPredicates.addAll(amountPredicates);
        validationPredicates.addAll(itemPredicates);
        return validationPredicates;
    }

    @Test
    void givenPremiumCustomer_withAmount100_andCoupon10_whenProcessingOrder_thenReturnsCorrectDiscount() {
        // Arrange
        Order order = new Order(
                "124",
                CustomerType.PREMIUM,
                new BigDecimal("100.00"),
                1,
                Optional.empty()
        );

        List<java.util.function.Predicate<Order>> validationPredicates = List.of(
                new IdNotNullPredicate(),
                new PositiveAmountPredicate(),
                new PositiveItemCountPredicate()
        );

        // Act
        DiscountResult result = pipeline.processOrder(
                order,
                regularDiscount,
                premiumDiscount,
                corporateDiscount,
                validationPredicates,
                Optional.of(new BigDecimal("10.00")) // $10 coupon
        );

        // Assert
        assertEquals(new BigDecimal("100.00"), result.originalAmount());
        // Premium: 10% of 100 = 10.0000
        // Coupon: 10.00 flat
        // Total discount: 20.0000
        // Final amount: 80.0000
        assertEquals(new BigDecimal("20.0000"), result.discountAmount());
        assertEquals(new BigDecimal("80.0000"), result.finalAmount());
    }

    @Test
    void givenCorporateCustomer_withAmount50_andCoupon60_whenProcessingOrder_thenReturnsZeroFinalAmount() {
        // Arrange
        Order order = new Order(
                "125",
                CustomerType.CORPORATE,
                new BigDecimal("50.00"),
                3,
                Optional.empty()
        );

        List<java.util.function.Predicate<Order>> validationPredicates = List.of(
                new IdNotNullPredicate(),
                new PositiveAmountPredicate(),
                new PositiveItemCountPredicate()
        );

        // Act
        DiscountResult result = pipeline.processOrder(
                order,
                regularDiscount,
                premiumDiscount,
                corporateDiscount,
                validationPredicates,
                Optional.of(new BigDecimal("60.00")) // $60 coupon on $50 order
        );

        // Assert
        assertEquals(new BigDecimal("50.00"), result.originalAmount());
        // Corporate: 15% of 50 = 7.5000
        // Amount after customer discount = 42.5000
        // Coupon: $60.00 but limited to 42.5000 (can't go below zero)
        // Final amount: 0.0000
        // Total discount: 50.0000
        assertEquals(new BigDecimal("0.0000"), result.finalAmount());
        assertEquals(new BigDecimal("50.0000"), result.discountAmount());
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

        List<java.util.function.Predicate<Order>> validationPredicates = List.of(
                new IdNotNullPredicate(),
                new PositiveAmountPredicate(),
                new PositiveItemCountPredicate()
        );

        assertThrows(IllegalStateException.class, () ->
                pipeline.processOrder(
                        order,
                        regularDiscount,
                        premiumDiscount,
                        corporateDiscount,
                        validationPredicates,
                        Optional.empty()
                )
        );
    }

    @Test
    void givenValidPredicatesList_whenCombiningWithAnd_thenReturnsCorrectCombinedPredicate() {
        List<java.util.function.Predicate<Order>> predicates = List.of(
                new IdNotNullPredicate(),
                new PositiveAmountPredicate(),
                new PositiveItemCountPredicate()
        );

        java.util.function.Predicate<Order> combined = DiscountPipeline.combineAnd(predicates);

        Order validOrder = new Order(
                "123",
                CustomerType.REGULAR,
                new BigDecimal("100.00"),
                2,
                Optional.empty()
        );

        Order invalidIdOrder = new Order(
                "",
                CustomerType.REGULAR,
                new BigDecimal("100.00"),
                2,
                Optional.empty()
        );

        Order invalidAmountOrder = new Order(
                "123",
                CustomerType.REGULAR,
                BigDecimal.ZERO,
                2,
                Optional.empty()
        );

        assertTrue(combined.test(validOrder));
        assertFalse(combined.test(invalidIdOrder));
        assertFalse(combined.test(invalidAmountOrder));
    }
}