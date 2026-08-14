# Repository Guidance

## Scope and instruction routing

These instructions apply to the entire repository. Read the relevant feature contract in
`docs/specs/` before changing behavior, and follow `.guidelines/java.md` for implementation
and test conventions. Reusable workflows live in `.agents/skills/`.

## Boundaries

- `src/main/java/org/example/price/domain`: provider contracts and immutable result values.
- `src/main/java/org/example/price/service`: asynchronous comparison orchestration.
- `src/main/java/org/example/price/provider`: simulated provider implementations.
- `src/main/java/org/example/price/exception`: comparison-specific failures.
- `src/test/java`: Given-When-Then behavior and concurrency verification.
- `docs/specs`: authoritative feature contracts and acceptance criteria.
- `.guidelines`: stable engineering conventions.

Keep provider integrations behind `PriceProvider`. Keep orchestration in
`PriceComparisonService`; providers must not coordinate with one another.

## Safe working and verification

- Preserve unrelated changes and never commit generated `target/` content.
- Prefer immutable values, explicit executor ownership, and bounded waits.
- Do not use raw `Thread` creation, the common pool, random test behavior, or timing-only
  concurrency proofs.
- Keep tests in Given-When-Then form. Comment complex coordination, not self-explanatory code.
- Run `mvn verify` after behavior changes. Do not commit or push unless explicitly requested.
