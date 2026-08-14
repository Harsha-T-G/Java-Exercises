package org.example.price.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.example.price.domain.PriceComparisonResult;
import org.example.price.domain.PriceProvider;
import org.example.price.domain.ProviderResult;
import org.example.price.domain.ProviderStatus;
import org.example.price.exception.PriceComparisonException;
import org.example.price.provider.ProviderBehavior;
import org.example.price.provider.SimulatedPriceProvider;
import org.junit.jupiter.api.Test;

class PriceComparisonServiceTest {

    @Test
    void givenAllProvidersSucceed_whenPricesAreCompared_thenReturnsLowestPrice() {
        // Given
        List<PriceProvider> providers = List.of(
                success("QuickQuote", "109.99", 20),
                success("BudgetBuy", "79.50", 60),
                success("MarketPrice", "94.00", 40));

        try (PriceComparisonService service = new PriceComparisonService(
                providers, Duration.ofMillis(500))) {
            // When
            PriceComparisonResult result = service.comparePrices();

            // Then
            assertEquals("BudgetBuy", result.providerName());
            assertEquals(0, new BigDecimal("79.50").compareTo(result.price()));
            assertEquals(3, result.providerResults().size());
            assertTrue(result.providerResults().stream().allMatch(ProviderResult::isSuccess));
            assertFalse(result.comparisonDuration().isNegative());
        }
    }

    @Test
    void givenOneProviderFails_whenPricesAreCompared_thenReturnsLowestSuccessfulPrice() {
        // Given
        List<PriceProvider> providers = List.of(
                success("Steady", "90.00", 30),
                failing("Broken", 10),
                success("Cheapest", "70.00", 50));

        try (PriceComparisonService service = new PriceComparisonService(
                providers, Duration.ofMillis(500))) {
            // When
            PriceComparisonResult result = service.comparePrices();

            // Then
            ProviderResult failedResult = result.resultFor("Broken");
            assertEquals("Cheapest", result.providerName());
            assertEquals(ProviderStatus.FAILED, failedResult.status());
            assertTrue(failedResult.detail().orElseThrow().contains("simulated failure"));
        }
    }

    @Test
    void givenOneProviderTimesOut_whenPricesAreCompared_thenExcludesTimedOutPrice() {
        // Given
        List<PriceProvider> providers = List.of(
                success("TooSlowButCheap", "1.00", 500),
                success("Available", "80.00", 20),
                success("Other", "100.00", 30));

        try (PriceComparisonService service = new PriceComparisonService(
                providers, Duration.ofMillis(100))) {
            // When
            PriceComparisonResult result = service.comparePrices();

            // Then
            assertEquals("Available", result.providerName());
            assertEquals(ProviderStatus.TIMED_OUT, result.resultFor("TooSlowButCheap").status());
            assertTrue(result.comparisonDuration().compareTo(Duration.ofMillis(400)) < 0,
                    "comparison should stop waiting at the timeout boundary");
        }
    }

    @Test
    void givenFastestProviderIsNotCheapest_whenPricesAreCompared_thenWaitsForCheapestProvider() {
        // Given
        List<PriceProvider> providers = List.of(
                success("FastExpensive", "120.00", 10),
                success("SlowCheapest", "75.00", 100),
                success("Medium", "95.00", 40));

        try (PriceComparisonService service = new PriceComparisonService(
                providers, Duration.ofMillis(500))) {
            // When
            PriceComparisonResult result = service.comparePrices();

            // Then
            assertEquals("SlowCheapest", result.providerName());
            assertTrue(result.comparisonDuration().compareTo(Duration.ofMillis(70)) >= 0,
                    "comparison must wait for all terminal outcomes");
        }
    }

    @Test
    void givenAllProvidersFailOrTimeout_whenPricesAreCompared_thenReportsEveryFailure() {
        // Given
        List<PriceProvider> providers = List.of(
                failing("BrokenOne", 10),
                success("TooSlow", "5.00", 500),
                failing("BrokenTwo", 20));

        try (PriceComparisonService service = new PriceComparisonService(
                providers, Duration.ofMillis(80))) {
            // When
            PriceComparisonException failure = assertThrows(
                    PriceComparisonException.class, service::comparePrices);

            // Then
            assertTrue(failure.getMessage().contains("No price provider succeeded"));
            assertTrue(failure.getMessage().contains("BrokenOne=FAILED"));
            assertTrue(failure.getMessage().contains("TooSlow=TIMED_OUT"));
            assertEquals(3, failure.providerResults().size());
            assertFalse(failure.comparisonDuration().isNegative());
        }
    }

    @Test
    void givenThreeBlockedProviders_whenComparisonStarts_thenEveryProviderRunsConcurrently()
            throws Exception {
        // Given: every provider reports its start, then blocks until the test releases all of them.
        CountDownLatch allProvidersStarted = new CountDownLatch(3);
        CountDownLatch releaseProviders = new CountDownLatch(1);
        List<PriceProvider> providers = List.of(
                coordinated("One", "30.00", allProvidersStarted, releaseProviders),
                coordinated("Two", "20.00", allProvidersStarted, releaseProviders),
                coordinated("Three", "10.00", allProvidersStarted, releaseProviders));
        ExecutorService comparisonCaller = Executors.newSingleThreadExecutor();

        try (PriceComparisonService service = new PriceComparisonService(
                providers, Duration.ofSeconds(2))) {
            // When: start comparison separately so this test can inspect the blocked provider calls.
            CompletableFuture<PriceComparisonResult> comparison =
                    CompletableFuture.supplyAsync(service::comparePrices, comparisonCaller);

            // Then: all calls must start before any call is released, proving overlap without timing math.
            assertTrue(allProvidersStarted.await(1, TimeUnit.SECONDS),
                    "all three calls should start before any provider is released");
            releaseProviders.countDown();
            assertEquals("Three", comparison.get(1, TimeUnit.SECONDS).providerName());
        } finally {
            releaseProviders.countDown();
            comparisonCaller.shutdownNow();
        }
    }

    @Test
    void givenServiceOwnsExecutor_whenServiceCloses_thenExecutorIsShutdown() {
        // Given
        ExecutorService executor = Executors.newFixedThreadPool(3);
        PriceComparisonService service = new PriceComparisonService(
                List.of(
                        success("One", "1.00", 0),
                        success("Two", "2.00", 0),
                        success("Three", "3.00", 0)),
                Duration.ofSeconds(1),
                executor);

        // When
        service.close();

        // Then
        assertTrue(executor.isShutdown());
    }

    private static PriceProvider success(String name, String price, long delayMillis) {
        return new SimulatedPriceProvider(
                name, new BigDecimal(price), Duration.ofMillis(delayMillis), ProviderBehavior.SUCCESS);
    }

    private static PriceProvider failing(String name, long delayMillis) {
        return new SimulatedPriceProvider(
                name, BigDecimal.ZERO, Duration.ofMillis(delayMillis), ProviderBehavior.FAILURE);
    }

    private static PriceProvider coordinated(
            String name,
            String price,
            CountDownLatch allProvidersStarted,
            CountDownLatch releaseProviders) {
        return new PriceProvider() {
            @Override
            public String name() {
                return name;
            }

            @Override
            public BigDecimal fetchPrice() throws InterruptedException {
                allProvidersStarted.countDown();
                if (!releaseProviders.await(1, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("providers were not released");
                }
                return new BigDecimal(price);
            }
        };
    }
}
