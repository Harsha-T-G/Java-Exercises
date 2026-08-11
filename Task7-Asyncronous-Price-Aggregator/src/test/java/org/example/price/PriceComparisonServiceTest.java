package org.example.price;

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
import org.junit.jupiter.api.Test;

class PriceComparisonServiceTest {

    @Test
    void returnsLowestPriceWhenAllProvidersSucceed() {
        List<PriceProvider> providers = List.of(
                success("QuickQuote", "109.99", 20),
                success("BudgetBuy", "79.50", 60),
                success("MarketPrice", "94.00", 40));

        try (PriceComparisonService service = new PriceComparisonService(
                providers, Duration.ofMillis(500))) {
            PriceComparisonResult result = service.comparePrices();

            assertEquals("BudgetBuy", result.providerName());
            assertEquals(0, new BigDecimal("79.50").compareTo(result.price()));
            assertEquals(3, result.providerResults().size());
            assertTrue(result.providerResults().stream().allMatch(ProviderResult::isSuccess));
            assertFalse(result.comparisonDuration().isNegative());
        }
    }

    @Test
    void isolatesOneProviderFailure() {
        List<PriceProvider> providers = List.of(
                success("Steady", "90.00", 30),
                failing("Broken", 10),
                success("Cheapest", "70.00", 50));

        try (PriceComparisonService service = new PriceComparisonService(
                providers, Duration.ofMillis(500))) {
            PriceComparisonResult result = service.comparePrices();

            assertEquals("Cheapest", result.providerName());
            ProviderResult broken = result.resultFor("Broken");
            assertEquals(ProviderStatus.FAILED, broken.status());
            assertTrue(broken.detail().orElseThrow().contains("simulated failure"));
        }
    }

    @Test
    void excludesOneProviderThatTimesOut() {
        List<PriceProvider> providers = List.of(
                success("TooSlowButCheap", "1.00", 500),
                success("Available", "80.00", 20),
                success("Other", "100.00", 30));

        try (PriceComparisonService service = new PriceComparisonService(
                providers, Duration.ofMillis(100))) {
            PriceComparisonResult result = service.comparePrices();

            assertEquals("Available", result.providerName());
            assertEquals(ProviderStatus.TIMED_OUT, result.resultFor("TooSlowButCheap").status());
            assertTrue(result.comparisonDuration().compareTo(Duration.ofMillis(400)) < 0,
                    "comparison should stop waiting at the timeout boundary");
        }
    }

    @Test
    void waitsForCheapestProviderInsteadOfReturningFastestResponse() {
        List<PriceProvider> providers = List.of(
                success("FastExpensive", "120.00", 10),
                success("SlowCheapest", "75.00", 100),
                success("Medium", "95.00", 40));

        try (PriceComparisonService service = new PriceComparisonService(
                providers, Duration.ofMillis(500))) {
            PriceComparisonResult result = service.comparePrices();

            assertEquals("SlowCheapest", result.providerName());
            assertTrue(result.comparisonDuration().compareTo(Duration.ofMillis(70)) >= 0,
                    "comparison must wait for all terminal outcomes");
        }
    }

    @Test
    void reportsMeaningfulFailureWhenAllProvidersFailOrTimeOut() {
        List<PriceProvider> providers = List.of(
                failing("BrokenOne", 10),
                success("TooSlow", "5.00", 500),
                failing("BrokenTwo", 20));

        try (PriceComparisonService service = new PriceComparisonService(
                providers, Duration.ofMillis(80))) {
            PriceComparisonException failure = assertThrows(
                    PriceComparisonException.class, service::comparePrices);

            assertTrue(failure.getMessage().contains("No price provider succeeded"));
            assertTrue(failure.getMessage().contains("BrokenOne=FAILED"));
            assertTrue(failure.getMessage().contains("TooSlow=TIMED_OUT"));
            assertEquals(3, failure.providerResults().size());
            assertFalse(failure.comparisonDuration().isNegative());
        }
    }

    @Test
    void startsEveryProviderConcurrently() throws Exception {
        CountDownLatch allStarted = new CountDownLatch(3);
        CountDownLatch releaseProviders = new CountDownLatch(1);
        List<PriceProvider> providers = List.of(
                coordinated("One", "30.00", allStarted, releaseProviders),
                coordinated("Two", "20.00", allStarted, releaseProviders),
                coordinated("Three", "10.00", allStarted, releaseProviders));
        ExecutorService comparisonCaller = Executors.newSingleThreadExecutor();

        try (PriceComparisonService service = new PriceComparisonService(
                providers, Duration.ofSeconds(2))) {
            CompletableFuture<PriceComparisonResult> comparison =
                    CompletableFuture.supplyAsync(service::comparePrices, comparisonCaller);

            assertTrue(allStarted.await(1, TimeUnit.SECONDS),
                    "all three calls should start before any provider is released");
            releaseProviders.countDown();

            assertEquals("Three", comparison.get(1, TimeUnit.SECONDS).providerName());
        } finally {
            releaseProviders.countDown();
            comparisonCaller.shutdownNow();
        }
    }

    @Test
    void closeShutsDownOwnedExecutor() {
        ExecutorService executor = Executors.newFixedThreadPool(3);
        PriceComparisonService service = new PriceComparisonService(
                List.of(
                        success("One", "1.00", 0),
                        success("Two", "2.00", 0),
                        success("Three", "3.00", 0)),
                Duration.ofSeconds(1),
                executor);

        service.close();

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
            CountDownLatch allStarted,
            CountDownLatch releaseProviders) {
        return new PriceProvider() {
            @Override
            public String name() {
                return name;
            }

            @Override
            public BigDecimal fetchPrice() throws InterruptedException {
                allStarted.countDown();
                if (!releaseProviders.await(1, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("providers were not released");
                }
                return new BigDecimal(price);
            }
        };
    }
}
