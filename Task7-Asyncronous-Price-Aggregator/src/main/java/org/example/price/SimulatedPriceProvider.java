package org.example.price;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Objects;

public final class SimulatedPriceProvider implements PriceProvider {
    private final String name;
    private final BigDecimal price;
    private final Duration delay;
    private final ProviderBehavior behavior;

    public SimulatedPriceProvider(
            String name, BigDecimal price, Duration delay, ProviderBehavior behavior) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Provider name must not be blank");
        }
        this.name = name;
        this.price = Objects.requireNonNull(price, "price");
        if (price.signum() < 0) {
            throw new IllegalArgumentException("Price must not be negative");
        }
        this.delay = Objects.requireNonNull(delay, "delay");
        if (delay.isNegative()) {
            throw new IllegalArgumentException("Delay must not be negative");
        }
        this.behavior = Objects.requireNonNull(behavior, "behavior");
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public BigDecimal fetchPrice() throws InterruptedException {
        Thread.sleep(delay.toMillis());
        if (behavior == ProviderBehavior.FAILURE) {
            throw new IllegalStateException(name + " simulated failure");
        }
        return price;
    }
}
