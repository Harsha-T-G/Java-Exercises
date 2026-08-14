package org.example.service;

import org.example.model.DiscountResult;
import org.example.model.Order;
import org.example.pipeline.CorporateCustomerDiscount;
import org.example.pipeline.DiscountPipeline;
import org.example.pipeline.IdPresentPredicate;
import org.example.pipeline.PositiveAmountPredicate;
import org.example.pipeline.PositiveItemCountPredicate;
import org.example.pipeline.PremiumCustomerDiscount;
import org.example.pipeline.RegularCustomerDiscount;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class OrderProcessingService {

    private static final Map<String, BigDecimal> PERCENTAGE_COUPONS = Map.of(
            "SAVE10", new BigDecimal("0.10"),
            "SAVE20", new BigDecimal("0.20")
    );
    private static final Map<String, BigDecimal> FIXED_COUPONS = Map.of(
            "FREE20", new BigDecimal("20.00")
    );

    private final DiscountPipeline pipeline;
    private final RegularCustomerDiscount regularDiscount;
    private final PremiumCustomerDiscount premiumDiscount;
    private final CorporateCustomerDiscount corporateDiscount;

    public OrderProcessingService() {
        this.pipeline = new DiscountPipeline();
        this.regularDiscount = new RegularCustomerDiscount();
        this.premiumDiscount = new PremiumCustomerDiscount();
        this.corporateDiscount = new CorporateCustomerDiscount();
    }

    public DiscountResult processOrder(Order order) {
        Objects.requireNonNull(order, "Order cannot be null");
        return processOrder(order, order.couponCode());
    }

    public DiscountResult processOrder(Order order, Optional<String> couponCode) {
        Objects.requireNonNull(order, "Order cannot be null");
        Objects.requireNonNull(couponCode, "Coupon code cannot be null");

        List<java.util.function.Predicate<Order>> validationPredicates = List.of(
                new IdPresentPredicate(),
                new PositiveAmountPredicate(),
                new PositiveItemCountPredicate());

        BigDecimal customerDiscount = DiscountPipeline.applyCustomerDiscount(
                order, regularDiscount, premiumDiscount, corporateDiscount);
        BigDecimal amountAfterCustomerDiscount = order.amount().subtract(customerDiscount);
        Optional<BigDecimal> couponDiscount = couponCode.map(
                code -> resolveCouponDiscount(code, amountAfterCustomerDiscount));

        // Process the order through the pipeline
        return pipeline.processOrder(
                order,
                regularDiscount,
                premiumDiscount,
                corporateDiscount,
                validationPredicates,
                couponDiscount
        );
    }

    private static BigDecimal resolveCouponDiscount(String code, BigDecimal eligibleAmount) {
        Objects.requireNonNull(code, "Coupon code cannot be null");
        BigDecimal fixedDiscount = FIXED_COUPONS.get(code);
        if (fixedDiscount != null) {
            return fixedDiscount;
        }
        BigDecimal rate = PERCENTAGE_COUPONS.get(code);
        if (rate != null) {
            return eligibleAmount.multiply(rate);
        }
        throw new IllegalArgumentException("Unknown coupon code: " + code);
    }
}
