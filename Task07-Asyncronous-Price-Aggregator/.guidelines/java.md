# Java Engineering Guidelines

- Target Java 17 and keep the project framework-free unless the specification changes.
- Use package boundaries to separate provider contracts, comparison results, and orchestration.
- Represent money with `BigDecimal` and durations with `Duration`.
- Prefer records and immutable collections for result values.
- Inject dependencies through constructors and validate public inputs at boundaries.
- Use `ExecutorService` and `CompletableFuture` for asynchronous work; never create raw threads.
- Give every external-style operation a bounded timeout and preserve interruption status.
- Convert expected provider failures into explicit outcomes before aggregating futures.
- Tests must be deterministic. Use latches or barriers to prove concurrency; use generous timing
  bounds only as supporting assertions.
- Structure every test as Given-When-Then, keep each phase visibly separated, and test one
  scenario per method. Use phase comments when helpful and explanatory comments for complex
  coordination or multi-stage setup; do not comment obvious code.
- Name tests by observable behavior and include failure-path and resource-lifecycle coverage.
