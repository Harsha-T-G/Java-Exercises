package org.example.price;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

public final class PriceComparisonService implements AutoCloseable {
    private static final Duration SHUTDOWN_GRACE_PERIOD = Duration.ofSeconds(1);

    private final List<PriceProvider> providers;
    private final Duration providerTimeout;
    private final ExecutorService executor;

    public PriceComparisonService(List<PriceProvider> providers, Duration providerTimeout) {
        this(providers, providerTimeout, Executors.newFixedThreadPool(3));
    }

    /**
     * Creates a service that owns the supplied executor. Calling {@link #close()} shuts it down.
     */
    public PriceComparisonService(
            List<PriceProvider> providers,
            Duration providerTimeout,
            ExecutorService executor) {
        this.providers = validateProviders(providers);
        this.providerTimeout = validateTimeout(providerTimeout);
        this.executor = Objects.requireNonNull(executor, "executor");
    }

    public PriceComparisonResult comparePrices() {
        if (executor.isShutdown()) {
            throw new IllegalStateException("PriceComparisonService is closed");
        }

        long comparisonStarted = System.nanoTime();
        List<CompletableFuture<ProviderResult>> futures = providers.stream()
                .map(this::requestPrice)
                .toList();

        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();
        List<ProviderResult> results = futures.stream()
                .map(CompletableFuture::join)
                .toList();
        Duration comparisonDuration = elapsedSince(comparisonStarted);

        ProviderResult cheapest = results.stream()
                .filter(ProviderResult::isSuccess)
                .min(Comparator.comparing(ProviderResult::price))
                .orElseThrow(() -> allProvidersFailed(results, comparisonDuration));

        return new PriceComparisonResult(
                cheapest.providerName(),
                cheapest.price(),
                comparisonDuration,
                results);
    }

    private CompletableFuture<ProviderResult> requestPrice(PriceProvider provider) {
        long callStarted = System.nanoTime();
        return CompletableFuture
                .supplyAsync(() -> fetchValidatedPrice(provider), executor)
                .orTimeout(providerTimeout.toNanos(), TimeUnit.NANOSECONDS)
                .handle((price, failure) -> toProviderResult(provider, price, failure, callStarted));
    }

    private BigDecimal fetchValidatedPrice(PriceProvider provider) {
        try {
            BigDecimal price = Objects.requireNonNull(
                    provider.fetchPrice(), provider.name() + " returned a null price");
            if (price.signum() < 0) {
                throw new IllegalArgumentException(provider.name() + " returned a negative price");
            }
            return price;
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw new CompletionException(interrupted);
        } catch (Exception failure) {
            throw new CompletionException(failure);
        }
    }

    private ProviderResult toProviderResult(
            PriceProvider provider,
            BigDecimal price,
            Throwable failure,
            long callStarted) {
        Duration duration = elapsedSince(callStarted);
        if (failure == null) {
            return ProviderResult.success(provider.name(), price, duration);
        }

        Throwable cause = unwrap(failure);
        if (cause instanceof TimeoutException) {
            return ProviderResult.failure(
                    provider.name(),
                    ProviderStatus.TIMED_OUT,
                    "Timed out after " + providerTimeout.toMillis() + " ms",
                    duration);
        }

        String detail = cause.getMessage() == null
                ? cause.getClass().getSimpleName()
                : cause.getMessage();
        return ProviderResult.failure(
                provider.name(), ProviderStatus.FAILED, detail, duration);
    }

    private static Throwable unwrap(Throwable failure) {
        Throwable current = failure;
        while ((current instanceof CompletionException)
                && current.getCause() != null) {
            current = current.getCause();
        }
        return current;
    }

    private static PriceComparisonException allProvidersFailed(
            List<ProviderResult> results, Duration duration) {
        String summary = results.stream()
                .map(result -> result.providerName() + "=" + result.status()
                        + " (" + result.detail().orElse("no detail") + ")")
                .collect(Collectors.joining(", "));
        return new PriceComparisonException(
                "No price provider succeeded. Outcomes: " + summary, results, duration);
    }

    private static List<PriceProvider> validateProviders(List<PriceProvider> providers) {
        Objects.requireNonNull(providers, "providers");
        if (providers.size() != 3) {
            throw new IllegalArgumentException("Exactly three price providers are required");
        }

        List<PriceProvider> copy = new ArrayList<>(providers.size());
        Set<String> names = new HashSet<>();
        for (PriceProvider provider : providers) {
            Objects.requireNonNull(provider, "Provider must not be null");
            if (provider.name() == null || provider.name().isBlank()) {
                throw new IllegalArgumentException("Provider name must not be blank");
            }
            if (!names.add(provider.name())) {
                throw new IllegalArgumentException("Provider names must be unique: " + provider.name());
            }
            copy.add(provider);
        }
        return List.copyOf(copy);
    }

    private static Duration validateTimeout(Duration timeout) {
        Objects.requireNonNull(timeout, "providerTimeout");
        if (timeout.isZero() || timeout.isNegative()) {
            throw new IllegalArgumentException("Provider timeout must be positive");
        }
        return timeout;
    }

    private static Duration elapsedSince(long startedAt) {
        return Duration.ofNanos(System.nanoTime() - startedAt);
    }

    @Override
    public void close() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(
                    SHUTDOWN_GRACE_PERIOD.toMillis(), TimeUnit.MILLISECONDS)) {
                executor.shutdownNow();
                executor.awaitTermination(
                        SHUTDOWN_GRACE_PERIOD.toMillis(), TimeUnit.MILLISECONDS);
            }
        } catch (InterruptedException interrupted) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
