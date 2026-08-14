package org.example.model;

import org.example.constants.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;


public class Order {
    private final String id;
    private final String customerId;
    private final String category;
    private final BigDecimal orderAmount;
    private final OrderStatus status;
    private final LocalDate date;

    public Order(String id, String customerId, String category, BigDecimal orderAmount, OrderStatus status, LocalDate date) {
        this.id = Objects.requireNonNull(id);
        this.customerId = Objects.requireNonNull(customerId);
        this.category = Objects.requireNonNull(category);
        this.orderAmount = Objects.requireNonNull(orderAmount);
        this.status = Objects.requireNonNull(status);
        this.date = Objects.requireNonNull(date);
    }

    public String getId() {
        return id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getCategory() {
        return category;
    }

    public BigDecimal getOrderAmount() {
        return orderAmount;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public LocalDate getDate() {
        return date;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return Objects.equals(id, order.id) &&
                Objects.equals(customerId, order.customerId) &&
                Objects.equals(category, order.category) &&
                Objects.equals(orderAmount, order.orderAmount) &&
                status == order.status &&
                Objects.equals(date, order.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, customerId, category, orderAmount, status, date);
    }

    @Override
    public String toString() {
        return "Order{" +
                "id='" + id + '\'' +
                ", customerId='" + customerId + '\'' +
                ", category='" + category + '\'' +
                ", orderAmount=" + orderAmount +
                ", status=" + status +
                ", date=" + date +
                '}';
    }

}