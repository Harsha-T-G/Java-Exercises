package org.example;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import org.example.price.PriceComparisonResult;
import org.example.price.PriceComparisonService;
import org.example.price.PriceProvider;
import org.example.price.ProviderBehavior;
import org.example.price.ProviderResult;
import org.example.price.SimulatedPriceProvider;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        List<PriceProvider> providers = List.of(
                new SimulatedPriceProvider(
                        "QuickQuote", new BigDecimal("109.99"),
                        Duration.ofMillis(100), ProviderBehavior.SUCCESS),
                new SimulatedPriceProvider(
                        "BudgetBuy", new BigDecimal("79.50"),
                        Duration.ofMillis(350), ProviderBehavior.SUCCESS),
                new SimulatedPriceProvider(
                        "UnstableMarket", new BigDecimal("89.00"),
                        Duration.ofMillis(200), ProviderBehavior.FAILURE));

        try (PriceComparisonService service = new PriceComparisonService(
                providers, Duration.ofMillis(500))) {
            PriceComparisonResult result = service.comparePrices();
            System.out.printf(
                    "Lowest price: %s from %s (comparison took %d ms)%n",
                    result.price(),
                    result.providerName(),
                    result.comparisonDuration().toMillis());
            result.providerResults().forEach(Main::printOutcome);
        }
    }

    private static void printOutcome(ProviderResult result) {
        String value = result.isSuccess()
                ? result.price().toPlainString()
                : result.detail().orElse("unknown failure");
        System.out.printf(
                "- %s: %s [%s] in %d ms%n",
                result.providerName(), value, result.status(), result.duration().toMillis());
    }
}
