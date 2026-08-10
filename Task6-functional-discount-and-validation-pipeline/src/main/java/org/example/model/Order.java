package org.example.model;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

public record Order(String id, CustomerType customerType, BigDecimal amount, int itemCount,
                    Optional<String> couponCode) {
    public Order(String id, CustomerType customerType, BigDecimal amount, int itemCount, Optional<String> couponCode) {
        this.id = Objects.requireNonNull(id, "Order ID must not be null");
        this.customerType = Objects.requireNonNull(customerType, "Customer type must not be null");
        this.amount = Objects.requireNonNull(amount, "Amount must not be null");
        this.itemCount = itemCount;
        this.couponCode = Objects.requireNonNullElseGet(couponCode, Optional::empty);
    }

    @Override
    public String toString() {
        return "Order{" +
                "id='" + id + '\'' +
                ", customerType=" + customerType +
                ", amount=" + amount +
                ", itemCount=" + itemCount +
                ", couponCode=" + couponCode +
                '}';
    }
}