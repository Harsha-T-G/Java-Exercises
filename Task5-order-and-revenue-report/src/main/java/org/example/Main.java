package org.example;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.Optional;
import org.example.model.Order;
import org.example.constants.OrderStatus;
import org.example.service.OrderReportService;

public class Main {
    public static void main(String[] args) {
        OrderReportService reportService = new OrderReportService();

        // Create sample orders
        List<Order> orders = Arrays.asList(
                new Order("1", "C1", "Electronics", new BigDecimal("1500.00"), OrderStatus.COMPLETED, LocalDate.of(2026, 1, 15)),
                new Order("2", "C1", "Books", new BigDecimal("50.00"), OrderStatus.COMPLETED, LocalDate.of(2026, 1, 20)),
                new Order("3", "C2", "Electronics", new BigDecimal("2000.00"), OrderStatus.COMPLETED, LocalDate.of(2026, 2, 10)),
                new Order("4", "C3", "Clothing", new BigDecimal("80.00"), OrderStatus.COMPLETED, LocalDate.of(2026, 2, 15)),
                new Order("5", "C2", "Electronics", new BigDecimal("12000.00"), OrderStatus.COMPLETED, LocalDate.of(2026, 3, 5)),
                new Order("6", "C1", "Books", new BigDecimal("75.50"), OrderStatus.CREATED, LocalDate.of(2026, 3, 10)),
                new Order("7", "C4", "Electronics", new BigDecimal("300.00"), OrderStatus.CANCELLED, LocalDate.of(2026, 3, 15)),
                new Order("8", "C3", "Clothing", new BigDecimal("120.00"), OrderStatus.COMPLETED, LocalDate.of(2026, 3, 20)),
                new Order("9", "C5", "Electronics", new BigDecimal("9500.00"), OrderStatus.COMPLETED, LocalDate.of(2026, 3, 25)),
                new Order("10", "C5", "Clothing", new BigDecimal("10000.00"), OrderStatus.COMPLETED, LocalDate.of(2026, 4, 1))
        );

        System.out.println("=== Order and Revenue Report ===\n");

        // 1. Total Revenue
        BigDecimal totalRevenue = reportService.calculateTotalRevenue(orders);
        System.out.println("1. Total Revenue: $" + totalRevenue);

        // 2. Revenue by Category
        Map<String, BigDecimal> revenueByCategory = reportService.calculateRevenueByCategory(orders);
        System.out.println("\n2. Revenue by Category:");
        revenueByCategory.forEach((category, revenue) ->
            System.out.println("   " + category + ": $" + revenue));

        // 3. Revenue by Customer
        Map<String, BigDecimal> revenueByCustomer = reportService.calculateRevenueByCustomer(orders);
        System.out.println("\n3. Revenue by Customer:");
        revenueByCustomer.forEach((customer, revenue) ->
            System.out.println("   " + customer + ": $" + revenue));

        // 4. Customer with Highest Value
        Optional<String> highestCustomer = reportService.findCustomerWithHighestValue(orders);
        System.out.println("\n4. Customer with Highest Value: " +
                (highestCustomer.map(s -> "$" + s).orElse("None")));

        // 5. Group Orders by Status
        Map<OrderStatus, List<Order>> ordersByStatus = reportService.groupOrdersByStatus(orders);
        System.out.println("\n5. Orders by Status:");
        ordersByStatus.forEach((status, orderList) ->
            System.out.println("   " + status + ": " + orderList.size() + " orders"));

        // 6. Partition Completed Orders (High Value >= 10000)
        Map<Boolean, List<Order>> partitionedOrders = reportService.partitionCompletedOrders(orders);
        System.out.println("\n6. Partitioned Completed Orders:");
        System.out.println("   High Value (>= $10000): " + partitionedOrders.get(true).size() + " orders");
        System.out.println("   Regular Value (< $10000): " + partitionedOrders.get(false).size() + " orders");

        // 7. Category with Highest Revenue
        Optional<String> highestCategory = reportService.findCategoryWithHighestRevenue(orders);
        System.out.println("\n7. Category with Highest Revenue: " +
                (highestCategory.map(s -> "$" + s).orElse("None")));

        // 8. Monthly Revenue Summary
        Map<String, BigDecimal> monthlyRevenue = reportService.monthlyRevenueSummary(orders);
        System.out.println("\n8. Monthly Revenue Summary:");
        monthlyRevenue.forEach((month, revenue) ->
            System.out.println("   " + month + ": $" + revenue));

        // 9. Five Most Recent Completed Orders
        List<Order> recentOrders = reportService.findFiveMostRecentCompletedOrders(orders);
        System.out.println("\n9. Five Most Recent Completed Orders:");
        recentOrders.forEach(order ->
            System.out.println("   Order " + order.getId() + ": $" + order.getOrderAmount() +
                    " on " + order.getDate()));
    }
}