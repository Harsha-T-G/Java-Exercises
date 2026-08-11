---
name: test-driven-development
description: Implement or repair Java behavior through deterministic red-green-refactor tests, especially concurrency and failure handling.
---

# Test-driven development

1. Translate one specification behavior into a focused failing JUnit test.
2. For concurrency, use coordination primitives rather than narrow sleep-based thresholds.
3. Implement the smallest production change that passes the test.
4. Refactor only while the suite remains green, then run `mvn verify`.
