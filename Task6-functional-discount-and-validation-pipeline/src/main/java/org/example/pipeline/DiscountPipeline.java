package org.example.pipeline;

import org.example.model.Order;
import org.example.model.DiscountResult;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public class DiscountPipeline {

    public static Predicate<Order> combineAnd(List<Predicate<Order>> predicates) {
        Objects.requireNonNull(predicates, "Predicates list cannot be null");
        if (predicates.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("Predicates list cannot contain null");
        }
        return order -> predicates.stream().allMatch(predicate -> predicate.test(order));
    }

    public static BigDecimal applyCustomerDiscount(Order order,
                                                   Function<Order, BigDecimal> regularDiscount,
                                                   Function<Order, BigDecimal> premiumDiscount,
                                                   Function<Order, BigDecimal> corporateDiscount) {
        Objects.requireNonNull(order, "Order cannot be null");
        Objects.requireNonNull(regularDiscount, "Regular discount function cannot be null");
        Objects.requireNonNull(premiumDiscount, "Premium discount function cannot be null");
        Objects.requireNonNull(corporateDiscount, "Corporate discount function cannot be null");

        return switch (order.customerType()) {
            case REGULAR -> regularDiscount.apply(order);
            case PREMIUM -> premiumDiscount.apply(order);
            case CORPORATE -> corporateDiscount.apply(order);
        };
    }

    public static BigDecimal applyCouponDiscount(BigDecimal amountAfterCustomerDiscount,
                                                 Optional<BigDecimal> couponDiscount) {
        Objects.requireNonNull(amountAfterCustomerDiscount, "Amount after customer discount cannot be null");
        Objects.requireNonNull(couponDiscount, "Coupon discount optional cannot be null");

        if (couponDiscount.isPresent()) {
            BigDecimal couponValue = couponDiscount.get();
            if (couponValue.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Coupon discount cannot be negative");
            }
            // Ensure coupon discount doesn't make amount negative
            BigDecimal finalAmount = amountAfterCustomerDiscount.subtract(couponValue);
            // Preserve the scale of the amount when comparing with zero
            BigDecimal zeroForComparison = BigDecimal.ZERO.setScale(amountAfterCustomerDiscount.scale());
            return finalAmount.max(zeroForComparison);
        }
        return amountAfterCustomerDiscount;
    }

    public static BigDecimal calculateDiscountAmount(BigDecimal originalAmount, BigDecimal finalAmount) {
        Objects.requireNonNull(originalAmount, "Original amount cannot be null");
        Objects.requireNonNull(finalAmount, "Final amount cannot be null");
        return originalAmount.subtract(finalAmount).max(BigDecimal.ZERO);
    }

    public DiscountResult processOrder(Order order,
                                       Function<Order, BigDecimal> regularDiscount,
                                       Function<Order, BigDecimal> premiumDiscount,
                                       Function<Order, BigDecimal> corporateDiscount,
                                       List<Predicate<Order>> validationPredicates,
                                       Optional<BigDecimal> couponDiscount) {
        Objects.requireNonNull(order, "Order cannot be null");
        Objects.requireNonNull(regularDiscount, "Regular discount function cannot be null");
        Objects.requireNonNull(premiumDiscount, "Premium discount function cannot be null");
        Objects.requireNonNull(corporateDiscount, "Corporate discount function cannot be null");
        Objects.requireNonNull(validationPredicates, "Validation predicates list cannot be null");
        Objects.requireNonNull(couponDiscount, "Coupon discount optional cannot be null");

        validate(order, validationPredicates);

        BigDecimal customerDiscount = applyCustomerDiscount(order, regularDiscount, premiumDiscount, corporateDiscount);
        BigDecimal amountAfterCustomerDiscount = order.amount().subtract(customerDiscount);
        BigDecimal finalAmount = applyCouponDiscount(amountAfterCustomerDiscount, couponDiscount);
        BigDecimal totalDiscount = calculateDiscountAmount(order.amount(), finalAmount);
        int scale = order.amount().scale();

        return new DiscountResult(
                order.amount(),
                totalDiscount.setScale(scale, RoundingMode.HALF_UP),
                finalAmount.setScale(scale, RoundingMode.HALF_UP)
        );
    }

    private static void validate(Order order, List<Predicate<Order>> validationPredicates) {
        validationPredicates.stream()
                .filter(predicate -> !predicate.test(order))
                .findFirst()
                .ifPresent(predicate -> {
                    throw new IllegalStateException(
                            "Order validation failed: " + predicate.getClass().getSimpleName());
                });
    }
}
