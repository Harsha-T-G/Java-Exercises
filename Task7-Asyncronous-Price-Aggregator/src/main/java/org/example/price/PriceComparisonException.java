package org.example.price;

import java.time.Duration;
import java.util.List;

public final class PriceComparisonException extends RuntimeException {
    private final List<ProviderResult> providerResults;
    private final Duration comparisonDuration;

    public PriceComparisonException(
            String message, List<ProviderResult> providerResults, Duration comparisonDuration) {
        super(message);
        this.providerResults = List.copyOf(providerResults);
        this.comparisonDuration = comparisonDuration;
    }

    public List<ProviderResult> providerResults() {
        return providerResults;
    }

    public Duration comparisonDuration() {
        return comparisonDuration;
    }
}
