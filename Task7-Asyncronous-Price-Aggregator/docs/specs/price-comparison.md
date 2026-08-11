# Asynchronous Price Comparison Specification

## Objective

Compare three independent price providers concurrently and return the lowest successful quote
without allowing one provider failure or timeout to fail an otherwise successful comparison.

## Scope

- Exactly three providers are required for this exercise.
- Provider calls are simulated locally; no network or persistence integration is required.
- The comparison API is synchronous to its caller while its provider work runs concurrently.

## Behavioral contract

1. Submit all provider calls to an `ExecutorService` through `CompletableFuture`.
2. Apply the configured timeout independently to every provider call.
3. Normalize every call into `SUCCESS`, `FAILED`, or `TIMED_OUT`.
4. Wait until all calls have either completed, failed, or timed out.
5. Select the lowest successful `BigDecimal` price, regardless of response order.
6. Return the winning provider, price, all provider outcomes, and total comparison duration.
7. If no provider succeeds, throw `PriceComparisonException` with each provider's outcome and
   the total comparison duration.
8. The service owns its executor. Closing it must attempt orderly shutdown, escalate to immediate
   shutdown when necessary, and preserve interruption status.

## Validation and operational constraints

- Provider names must be non-blank and prices must be non-null and non-negative.
- The provider collection must contain exactly three entries with unique names.
- Timeout must be positive and the executor must be non-null.
- No raw threads, unbounded waiting, random failures, or reliance on the common fork-join pool.

## Acceptance tests

- All providers succeed and the cheapest quote wins.
- One provider fails and another successful provider still wins.
- One provider times out and another successful provider still wins.
- The fastest provider is not the cheapest and must not win merely by responding first.
- All providers fail or time out and a meaningful aggregate failure is returned.
- A latch-based test proves all calls start before any call is released, demonstrating concurrency.
- Closing the service shuts down its executor.

## Open decisions

- A production integration may need stronger cancellation for blocking I/O. Java's
  `CompletableFuture.orTimeout` stops waiting but does not guarantee interruption of the supplier.
