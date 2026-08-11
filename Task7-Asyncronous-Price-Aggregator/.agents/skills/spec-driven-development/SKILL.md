---
name: spec-driven-development
description: Define or update a feature contract before changing Java application behavior, public APIs, failure semantics, or operational constraints.
---

# Spec-driven development

1. Read the relevant file in `docs/specs/` and identify affected behavior.
2. Add or update objective, scope, observable contract, failure states, tests, and open decisions.
3. Keep implementation details out unless they are required constraints.
4. Confirm tests and code implement the approved contract without undocumented behavior.
