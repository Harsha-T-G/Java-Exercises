package org.example.price.domain;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

public record PriceComparisonResult(
        String providerName,
        BigDecimal price,
        Duration comparisonDuration,
        List<ProviderResult> providerResults) {

    public PriceComparisonResult {
        Objects.requireNonNull(providerName, "providerName");
        Objects.requireNonNull(price, "price");
        Objects.requireNonNull(comparisonDuration, "comparisonDuration");
        providerResults = List.copyOf(providerResults);
    }

    public ProviderResult resultFor(String name) {
        return providerResults.stream()
                .filter(result -> result.providerName().equals(name))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No result for provider: " + name));
    }
}
