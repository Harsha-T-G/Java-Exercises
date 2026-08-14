package org.example.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import org.example.model.Order;
import org.example.constants.OrderStatus;

/**
 * Test class for OrderReportService.
 */
class OrderReportServiceTest {

    private OrderReportService orderReportService;
    private List<Order> testOrders;

    @BeforeEach
    void setUp() {
        orderReportService = new OrderReportService();

        // Arrange: Create test data with varied customers, categories, amounts, dates, and statuses
        testOrders = Arrays.asList(
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
    }

    @Test
    void givenCompletedOrders_whenCalculatingTotalRevenue_thenReturnsCorrectSum() {
        // Act
        BigDecimal total = orderReportService.calculateTotalRevenue(testOrders);

        // Assert
        // Expected: 1500 + 50 + 2000 + 80 + 12000 + 120 + 9500 + 10000 = 35250
        assertEquals(new BigDecimal("35250.00"), total);
    }

    @Test
    void givenEmptyOrderList_whenCalculatingTotalRevenue_thenReturnsZero() {
        // Act
        BigDecimal total = orderReportService.calculateTotalRevenue(Collections.emptyList());

        // Assert
        assertEquals(BigDecimal.ZERO, total);
    }

    @Test
    void givenNoCompletedOrders_whenCalculatingTotalRevenue_thenReturnsZero() {
        // Arrange
        List<Order> noCompletedOrders = Arrays.asList(
                new Order("1", "C1", "Electronics", new BigDecimal("100.00"), OrderStatus.CREATED, LocalDate.of(2026, 1, 1)),
                new Order("2", "C2", "Books", new BigDecimal("50.00"), OrderStatus.CANCELLED, LocalDate.of(2026, 1, 2))
        );

        // Act
        BigDecimal total = orderReportService.calculateTotalRevenue(noCompletedOrders);

        // Assert
        assertEquals(BigDecimal.ZERO, total);
    }

    @Test
    void givenCompletedOrders_whenCalculatingRevenueByCategory_thenReturnsCorrectMap() {
        // Act
        Map<String, BigDecimal> revenueByCategory = orderReportService.calculateRevenueByCategory(testOrders);

        // Assert
        // Electronics: 1500 + 2000 + 12000 + 9500 = 25000
        // Books: 50 (only order 2 is completed, order 6 is CREATED)
        // Clothing: 80 + 120 + 10000 = 10200

        assertEquals(new BigDecimal("25000.00"), revenueByCategory.get("Electronics"));
        assertEquals(new BigDecimal("50.00"), revenueByCategory.get("Books"));
        assertEquals(new BigDecimal("10200.00"), revenueByCategory.get("Clothing"));
        assertEquals(3, revenueByCategory.size());
    }

    @Test
    void givenCompletedOrders_whenCalculatingRevenueByCustomer_thenReturnsCorrectMap() {
        // Act
        Map<String, BigDecimal> revenueByCustomer = orderReportService.calculateRevenueByCustomer(testOrders);

        // Assert
        // C1: 1500 + 50 = 1550
        // C2: 2000 + 12000 = 14000
        // C3: 80 + 120 = 200
        // C5: 9500 + 10000 = 19500

        assertEquals(new BigDecimal("1550.00"), revenueByCustomer.get("C1"));
        assertEquals(new BigDecimal("14000.00"), revenueByCustomer.get("C2"));
        assertEquals(new BigDecimal("200.00"), revenueByCustomer.get("C3"));
        assertEquals(new BigDecimal("19500.00"), revenueByCustomer.get("C5"));
        assertEquals(4, revenueByCustomer.size());
    }

    @Test
    void givenCompletedOrders_whenFindingCustomerWithHighestValue_thenReturnsCorrectCustomer() {
        // Act
        java.util.Optional<String> highestCustomer = orderReportService.findCustomerWithHighestValue(testOrders);

        // Assert
        // C5 has highest: 19500
        assertEquals(java.util.Optional.of("C5"), highestCustomer);
    }

    @Test
    void givenNoCompletedOrders_whenFindingCustomerWithHighestValue_thenReturnsEmpty() {
        // Arrange
        List<Order> noCompletedOrders = Arrays.asList(
                new Order("1", "C1", "Electronics", new BigDecimal("100.00"), OrderStatus.CREATED, LocalDate.of(2026, 1, 1)),
                new Order("2", "C2", "Books", new BigDecimal("50.00"), OrderStatus.CANCELLED, LocalDate.of(2026, 1, 2))
        );

        // Act
        java.util.Optional<String> highestCustomer = orderReportService.findCustomerWithHighestValue(noCompletedOrders);

        // Assert
        assertEquals(java.util.Optional.empty(), highestCustomer);
    }

    @Test
    void givenMixedOrders_whenGroupingByStatus_thenReturnsCorrectGroups() {
        // Act
        Map<OrderStatus, List<Order>> grouped = orderReportService.groupOrdersByStatus(testOrders);

        // Assert
        // CREATED: order 6
        // COMPLETED: orders 1,2,3,4,5,8,9,10 (8 orders)
        // CANCELLED: order 7

        assertEquals(1, grouped.get(OrderStatus.CREATED).size());
        assertEquals(8, grouped.get(OrderStatus.COMPLETED).size());
        assertEquals(1, grouped.get(OrderStatus.CANCELLED).size());
    }

    @Test
    void givenCompletedOrders_whenPartitioningByValue_thenReturnsCorrectPartition() {
        // Act
        Map<Boolean, List<Order>> partitioned = orderReportService.partitionCompletedOrders(testOrders);

        // Assert
        // High-value (>= 10000): orders 5 (12000) and 10 (10000)
        // Regular-value (< 10000): orders 1 (1500), 2 (50), 3 (2000), 4 (80), 8 (120), 9 (9500)

        assertEquals(2, partitioned.get(true).size());
        assertEquals(6, partitioned.get(false).size());

        // Verify specific orders are in correct partitions
        List<Order> highValue = partitioned.get(true);
        List<Order> regularValue = partitioned.get(false);

        boolean hasOrder5 = highValue.stream().anyMatch(o -> o.getId().equals("5"));
        boolean hasOrder10 = highValue.stream().anyMatch(o -> o.getId().equals("10"));
        assertTrue(hasOrder5 && hasOrder10);

        boolean hasOrder1 = regularValue.stream().anyMatch(o -> o.getId().equals("1"));
        boolean hasOrder2 = regularValue.stream().anyMatch(o -> o.getId().equals("2"));
        boolean hasOrder3 = regularValue.stream().anyMatch(o -> o.getId().equals("3"));
        boolean hasOrder4 = regularValue.stream().anyMatch(o -> o.getId().equals("4"));
        boolean hasOrder8 = regularValue.stream().anyMatch(o -> o.getId().equals("8"));
        boolean hasOrder9 = regularValue.stream().anyMatch(o -> o.getId().equals("9"));
        assertTrue(hasOrder1 && hasOrder2 && hasOrder3 && hasOrder4 && hasOrder8 && hasOrder9);
    }

    @Test
    void givenCompletedOrders_whenFindingCategoryWithHighestRevenue_thenReturnsCorrectCategory() {
        // Act
        java.util.Optional<String> highestCategory = orderReportService.findCategoryWithHighestRevenue(testOrders);

        // Assert
        // Electronics: 25000 (highest)
        // Books: 50
        // Clothing: 10200
        assertEquals(java.util.Optional.of("Electronics"), highestCategory);
    }

    @Test
    void givenCompletedOrders_whenCalculatingMonthlyRevenue_thenReturnsCorrectChronologicalOrder() {
        // Act
        Map<String, BigDecimal> monthlyRevenue = orderReportService.monthlyRevenueSummary(testOrders);

        // Assert
        // Jan 2026: orders 1,2 = 1500 + 50 = 1550
        // Feb 2026: orders 3,4 = 2000 + 80 = 2080
        // Mar 2026: orders 5,8,9 = 12000 + 120 + 9500 = 21620
        // Apr 2026: order 10 = 10000

        assertEquals(new BigDecimal("1550.00"), monthlyRevenue.get("2026-01"));
        assertEquals(new BigDecimal("2080.00"), monthlyRevenue.get("2026-02"));
        assertEquals(new BigDecimal("21620.00"), monthlyRevenue.get("2026-03"));
        assertEquals(new BigDecimal("10000.00"), monthlyRevenue.get("2026-04"));

        // Verify chronological order
        List<String> months = new ArrayList<>(monthlyRevenue.keySet());
        assertEquals(Arrays.asList("2026-01", "2026-02", "2026-03", "2026-04"), months);
    }

    @Test
    void givenCompletedOrders_whenFindingFiveMostRecent_thenReturnsFiveMostRecent() {
        // Act
        List<Order> recentOrders = orderReportService.findFiveMostRecentCompletedOrders(testOrders);

        // Assert
        // Completed orders sorted by date descending:
        // 10 (Apr 1), 9 (Mar 25), 8 (Mar 20), 5 (Mar 5), 4 (Feb 15), 3 (Feb 10), 2 (Jan 20), 1 (Jan 15)
        // Top 5: 10, 9, 8, 5, 4

        assertEquals(5, recentOrders.size());
        assertEquals("10", recentOrders.get(0).getId());
        assertEquals("9", recentOrders.get(1).getId());
        assertEquals("8", recentOrders.get(2).getId());
        assertEquals("5", recentOrders.get(3).getId());
        assertEquals("4", recentOrders.get(4).getId());
    }

    @Test
    void givenLessThanFiveCompletedOrders_whenFindingFiveMostRecent_thenReturnsAllCompleted() {
        // Arrange
        List<Order> fewOrders = Arrays.asList(
                new Order("1", "C1", "Electronics", new BigDecimal("100.00"), OrderStatus.COMPLETED, LocalDate.of(2026, 1, 1)),
                new Order("2", "C2", "Books", new BigDecimal("50.00"), OrderStatus.COMPLETED, LocalDate.of(2026, 1, 2))
        );

        // Act
        List<Order> recentOrders = orderReportService.findFiveMostRecentCompletedOrders(fewOrders);

        // Assert
        assertEquals(2, recentOrders.size());
        assertEquals("2", recentOrders.get(0).getId()); // Most recent first
        assertEquals("1", recentOrders.get(1).getId());
    }
}