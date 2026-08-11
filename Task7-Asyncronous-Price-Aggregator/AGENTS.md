# Repository Guidance

## Scope and instruction routing

These instructions apply to the entire repository. Read the relevant feature contract in
`docs/specs/` before changing behavior, and follow `.guidelines/java.md` for implementation
and test conventions. Reusable workflows live in `.agents/skills/`.

## Boundaries

- `src/main/java`: production domain and concurrency code.
- `src/test/java`: deterministic behavior and concurrency verification.
- `docs/specs`: authoritative feature contracts and acceptance criteria.
- `.guidelines`: stable engineering conventions.

Keep provider integrations behind `PriceProvider`. Keep orchestration in
`PriceComparisonService`; providers must not coordinate with one another.

## Safe working and verification

- Preserve unrelated changes and never commit generated `target/` content.
- Prefer immutable values, explicit executor ownership, and bounded waits.
- Do not use raw `Thread` creation, the common pool, random test behavior, or timing-only
  concurrency proofs.
- Run `mvn verify` after behavior changes. Do not commit or push unless explicitly requested.
