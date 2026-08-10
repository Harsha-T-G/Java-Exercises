package org.example.service;

import org.example.model.DiscountResult;
import org.example.model.Order;
import org.example.pipeline.CorporateCustomerDiscount;
import org.example.pipeline.DiscountPipeline;
import org.example.pipeline.IdNotNullPredicate;
import org.example.pipeline.PositiveAmountPredicate;
import org.example.pipeline.PositiveItemCountPredicate;
import org.example.pipeline.PremiumCustomerDiscount;
import org.example.pipeline.RegularCustomerDiscount;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class OrderProcessingService {

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
        return processOrder(order, order.couponCode());
    }

    public DiscountResult processOrder(Order order, Optional<String> couponCode) {
        Objects.requireNonNull(order, "Order cannot be null");
        Objects.requireNonNull(couponCode, "Coupon code cannot be null");

        // Create validation predicates
        List<java.util.function.Predicate<Order>> validationPredicates = new ArrayList<>();
        validationPredicates.add(new IdNotNullPredicate());
        validationPredicates.add(new PositiveAmountPredicate());
        validationPredicates.add(new PositiveItemCountPredicate());


        // Calculate coupon discount if present (10% of amount after customer discount)
        Optional<BigDecimal> couponDiscount = Optional.empty();
        if (couponCode.isPresent()) {
            // Calculate customer discount first to determine amount after customer discount
            BigDecimal customerDiscount = switch (order.customerType()) {
                case REGULAR -> regularDiscount.apply(order);
                case PREMIUM -> premiumDiscount.apply(order);
                case CORPORATE -> corporateDiscount.apply(order);
            };
            BigDecimal amountAfterCustomerDiscount = order.amount().subtract(customerDiscount);
            // Coupon is 10% of amount after customer discount
            couponDiscount = Optional.of(amountAfterCustomerDiscount.multiply(new BigDecimal("0.10")));
        }

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
}