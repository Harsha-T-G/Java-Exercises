package org.example;

import org.example.model.CustomerType;
import org.example.model.DiscountResult;
import org.example.model.Order;
import org.example.service.OrderProcessingService;

import java.math.BigDecimal;
import java.util.Optional;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Functional Discount and Validation Pipeline Demo ===\n");


        OrderProcessingService orderProcessingService = new OrderProcessingService();

        // Create sample orders to demonstrate the functionality
        Order regularOrder = new Order(
                "ORD-001",
                CustomerType.REGULAR,
                new BigDecimal("100.00"),
                2,
                Optional.of("SAVE10")  // 10% coupon
        );

        Order premiumOrder = new Order(
                "ORD-002",
                CustomerType.PREMIUM,
                new BigDecimal("200.00"),
                5,
                Optional.empty()  // No coupon
        );

        Order corporateOrder = new Order(
                "ORD-003",
                CustomerType.CORPORATE,
                new BigDecimal("150.00"),
                10,
                Optional.of("FREE20")  // $20 coupon
        );

        // Process the orders and display results
        System.out.println("Processing Regular Customer Order:");
        processAndDisplayOrder(orderProcessingService, regularOrder);

        System.out.println("\nProcessing Premium Customer Order:");
        processAndDisplayOrder(orderProcessingService, premiumOrder);

        System.out.println("\nProcessing Corporate Customer Order:");
        processAndDisplayOrder(orderProcessingService, corporateOrder);


        System.out.println("\nProcessing Invalid Order (negative amount):");
        Order invalidOrder = new Order(
                "ORD-004",
                CustomerType.REGULAR,
                new BigDecimal("-50.00"),  // Invalid: negative amount
                3,
                Optional.empty()
        );

        try {
            orderProcessingService.processOrder(invalidOrder);
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("Order rejected as expected: " + e.getMessage());
        }

        System.out.println("\n=== Demo Complete ===");
    }

    private static void processAndDisplayOrder(OrderProcessingService service, Order order) {
        try {
            DiscountResult result = service.processOrder(order, order.couponCode());
            System.out.println("  Order ID: " + order.id());
            System.out.println("  Customer Type: " + order.customerType());
            System.out.println("  Original Amount: $" + result.originalAmount());
            System.out.println("  Discount Amount: $" + result.discountAmount());
            System.out.println("  Final Amount: $" + result.finalAmount());
            if (order.couponCode().isPresent()) {
                System.out.println("  Coupon Applied: " + order.couponCode().get());
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("  Error processing order: " + e.getMessage());
        }
    }
}
