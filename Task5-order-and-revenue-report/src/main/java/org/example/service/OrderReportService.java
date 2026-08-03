package org.example.service;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import org.example.model.Order;
import org.example.constants.OrderStatus;


public class OrderReportService {

    public BigDecimal calculateTotalRevenue(List<Order> orders) {
        if (orders == null || orders.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return orders.stream()
                .filter(order -> OrderStatus.COMPLETED.equals(order.getStatus()))
                .map(Order::getOrderAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Map<String, BigDecimal> calculateRevenueByCategory(List<Order> orders) {
        if (orders == null || orders.isEmpty()) {
            return Collections.emptyMap();
        }
        return orders.stream()
                .filter(order -> OrderStatus.COMPLETED.equals(order.getStatus()))
                .collect(Collectors.groupingBy(
                        Order::getCategory,
                        Collectors.reducing(BigDecimal.ZERO, Order::getOrderAmount, BigDecimal::add)
                ));
    }

    public Map<String, BigDecimal> calculateRevenueByCustomer(List<Order> orders) {
        if (orders == null || orders.isEmpty()) {
            return Collections.emptyMap();
        }
        return orders.stream()
                .filter(order -> OrderStatus.COMPLETED.equals(order.getStatus()))
                .collect(Collectors.groupingBy(
                        Order::getCustomerId,
                        Collectors.reducing(BigDecimal.ZERO, Order::getOrderAmount, BigDecimal::add)
                ));
    }

    public Optional<String> findCustomerWithHighestValue(List<Order> orders) {
        if (orders == null || orders.isEmpty()) {
            return Optional.empty();
        }
        return orders.stream()
                .filter(order -> OrderStatus.COMPLETED.equals(order.getStatus()))
                .collect(Collectors.groupingBy(
                        Order::getCustomerId,
                        Collectors.reducing(BigDecimal.ZERO, Order::getOrderAmount, BigDecimal::add)
                ))
                .entrySet()
                .stream()
                .max(Map.Entry.comparingByValue(Comparator.naturalOrder()))
                .map(Map.Entry::getKey);
    }

    public Map<OrderStatus, List<Order>> groupOrdersByStatus(List<Order> orders) {
        if (orders == null || orders.isEmpty()) {
            return Collections.emptyMap();
        }
        return orders.stream()
                .collect(Collectors.groupingBy(Order::getStatus));
    }

    public Map<Boolean, List<Order>> partitionCompletedOrders(List<Order> orders) {
        if (orders == null || orders.isEmpty()) {
            return Map.of(
                    true, Collections.emptyList(),
                    false, Collections.emptyList()
            );
        }
        return orders.stream()
                .filter(order -> OrderStatus.COMPLETED.equals(order.getStatus()))
                .collect(Collectors.partitioningBy(
                        order -> order.getOrderAmount().compareTo(BigDecimal.valueOf(10000)) >= 0
                ));
    }

    public Optional<String> findCategoryWithHighestRevenue(List<Order> orders) {
        if (orders == null || orders.isEmpty()) {
            return Optional.empty();
        }
        return orders.stream()
                .filter(order -> OrderStatus.COMPLETED.equals(order.getStatus()))
                .collect(Collectors.groupingBy(
                        Order::getCategory,
                        Collectors.reducing(BigDecimal.ZERO, Order::getOrderAmount, BigDecimal::add)
                ))
                .entrySet()
                .stream()
                .max(Map.Entry.comparingByValue(Comparator.naturalOrder()))
                .map(Map.Entry::getKey);
    }

    public Map<String, BigDecimal> monthlyRevenueSummary(List<Order> orders) {
        if (orders == null || orders.isEmpty()) {
            return Collections.emptyMap();
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");

        return orders.stream()
                .filter(order -> OrderStatus.COMPLETED.equals(order.getStatus()))
                .collect(Collectors.groupingBy(
                        order -> order.getDate().format(formatter),
                        TreeMap::new, // ensures natural ordering of keys (year-month)
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                Order::getOrderAmount,
                                BigDecimal::add
                        )
                ));
    }

    public List<Order> findFiveMostRecentCompletedOrders(List<Order> orders) {
        if (orders == null || orders.isEmpty()) {
            return Collections.emptyList();
        }
        return orders.stream()
                .filter(order -> OrderStatus.COMPLETED.equals(order.getStatus()))
                .sorted(Comparator.comparing(Order::getDate).reversed())
                .limit(5)
                .toList();
    }
}