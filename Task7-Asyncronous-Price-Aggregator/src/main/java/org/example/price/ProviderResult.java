package org.example.price;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;

public record ProviderResult(
        String providerName,
        ProviderStatus status,
        BigDecimal price,
        String failureDetail,
        Duration duration) {

    public ProviderResult {
        Objects.requireNonNull(providerName, "providerName");
        Objects.requireNonNull(status, "status");
        Objects.requireNonNull(duration, "duration");
        if (status == ProviderStatus.SUCCESS && price == null) {
            throw new IllegalArgumentException("A successful result requires a price");
        }
        if (status != ProviderStatus.SUCCESS && failureDetail == null) {
            throw new IllegalArgumentException("An unsuccessful result requires failure detail");
        }
    }

    public static ProviderResult success(String providerName, BigDecimal price, Duration duration) {
        return new ProviderResult(providerName, ProviderStatus.SUCCESS, price, null, duration);
    }

    public static ProviderResult failure(
            String providerName, ProviderStatus status, String detail, Duration duration) {
        if (status == ProviderStatus.SUCCESS) {
            throw new IllegalArgumentException("Failure status cannot be SUCCESS");
        }
        return new ProviderResult(providerName, status, null, detail, duration);
    }

    public boolean isSuccess() {
        return status == ProviderStatus.SUCCESS;
    }

    public Optional<BigDecimal> successfulPrice() {
        return Optional.ofNullable(price);
    }

    public Optional<String> detail() {
        return Optional.ofNullable(failureDetail);
    }
}
