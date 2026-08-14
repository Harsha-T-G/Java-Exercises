package org.example;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import org.example.price.domain.PriceComparisonResult;
import org.example.price.domain.PriceProvider;
import org.example.price.domain.ProviderResult;
import org.example.price.provider.ProviderBehavior;
import org.example.price.provider.SimulatedPriceProvider;
import org.example.price.service.PriceComparisonService;

public final class Main {

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
                        Duration.ofMillis(200), ProviderBehavior.FAILURE),
               new SimulatedPriceProvider(
                        "TimeOutBuy", new BigDecimal("29.99"),
                        Duration.ofMillis(600), ProviderBehavior.SUCCESS));

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
