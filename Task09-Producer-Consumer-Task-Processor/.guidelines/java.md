# Java Engineering Guidelines

- Target Java 17 and keep the project framework-free.
- Separate immutable domain/result values from producer-consumer orchestration.
- Inject processing behavior through constructors and validate public inputs at boundaries.
- Use `ExecutorService` and Java concurrency primitives; never use `Thread.stop`, raw busy-waiting, or the common pool.
- Give waits bounded timeouts and preserve interruption status at the boundary that consumes an interruption.
- Prefer records for immutable result values and defensive copies for exposed collections.
- Tests must be deterministic. Use latches or barriers to prove concurrency; timing bounds are supporting safeguards, not the proof itself.
- Structure every test as Given-When-Then, keep each phase visibly separated, and test one observable scenario per method.
- Name tests by behavior and include success, failure, interruption, backpressure, duplicate-processing, and resource-lifecycle coverage.
