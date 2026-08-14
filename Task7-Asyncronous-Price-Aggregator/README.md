# Asynchronous Price Aggregator

A Java 17 exercise demonstrating concurrent provider calls with `ExecutorService` and
`CompletableFuture`, per-provider timeouts, failure isolation, minimum-price selection, elapsed
time measurement, and explicit executor shutdown.

## Design

Each `PriceProvider` call is submitted before the service waits. Its future receives an independent
timeout and is then normalized into a `ProviderResult` (`SUCCESS`, `FAILED`, or `TIMED_OUT`). Since
expected failures become values, `CompletableFuture.allOf` can wait for every terminal outcome
without one provider failing the complete request. The service then selects the lowest successful
`BigDecimal` quote.

The service owns its executor, including an executor supplied through the testing/configuration
constructor, and implements `AutoCloseable` for deterministic cleanup.

## Project structure

```text
src/main/java/org/example
├── Main.java                         application entry point
└── price
    ├── domain                        contracts and immutable result values
    ├── exception                     comparison-specific exceptions
    ├── provider                      simulated provider implementations
    └── service                       asynchronous orchestration

src/test/java/org/example/price
└── service                           Given-When-Then service tests
```

This is a plain Java project, so it uses only meaningful packages. Spring-specific layers such as
`controller`, `repository`, `dto`, and `config` are intentionally omitted because no REST or
database capability exists.

## Run

```bash
mvn verify
java -cp target/classes org.example.Main
```

## Test evidence

The JUnit suite covers:

- all providers succeeding;
- one provider failing;
- one provider timing out;
- the fastest quote not being the cheapest;
- every provider failing or timing out;
- latch-based proof that all provider calls overlap;
- executor shutdown on service close.

Tests use Given-When-Then naming and visibly separated phases. The concurrency scenario includes
focused comments explaining its latch coordination; straightforward assertions are not
over-commented.

See [`docs/specs/price-comparison.md`](docs/specs/price-comparison.md) for the complete contract.
