---
name: verify-feature-readiness
description: Verify a Java feature against its specification, tests, build gates, resource lifecycle, and repository hygiene before handoff.
---

# Verify feature readiness

1. Compare every acceptance criterion in `docs/specs/` with executable test evidence.
2. Run `mvn verify` and investigate warnings or flaky timing assumptions.
3. Review executor shutdown, interruption, timeouts, exception causes, and immutable results.
4. Inspect `git status` and ensure generated files and unrelated changes are excluded.
5. Report verified behavior and any residual operational risk.
