package org.example.model;

import java.math.BigDecimal;
import java.util.Objects;

public record DiscountResult(BigDecimal originalAmount, BigDecimal discountAmount, BigDecimal finalAmount) {
    public DiscountResult(BigDecimal originalAmount, BigDecimal discountAmount, BigDecimal finalAmount) {
        this.originalAmount = Objects.requireNonNull(originalAmount, "Original amount cannot be null");
        this.discountAmount = Objects.requireNonNull(discountAmount, "Discount amount cannot be null");
        this.finalAmount = Objects.requireNonNull(finalAmount, "Final amount cannot be null");

        // Ensure final amount is not negative
        if (this.finalAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Final amount cannot be negative");
        }
        // Ensure discount amount is not negative
        if (this.discountAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Discount amount cannot be negative");
        }
        // Ensure discount amount does not exceed original amount
        if (this.discountAmount.compareTo(this.originalAmount) > 0) {
            throw new IllegalArgumentException("Discount amount cannot exceed original amount");
        }
        if (this.originalAmount.subtract(this.discountAmount).compareTo(this.finalAmount) != 0) {
            throw new IllegalArgumentException("Final amount must equal original amount minus discount amount");
        }
    }
}
