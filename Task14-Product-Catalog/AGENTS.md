# Product Catalog Agent Guidelines

## Scope and precedence

This file is the repository-wide source of truth for coding agents. Platform
instructions and the current user request apply first. Within this repository:

1. An approved `SPEC.md` and its linked `docs/specs/product-catalog/` chunks
   define product behavior. `SPEC.md` owns their shared approval status.
2. `CONTEXT.md` defines stable domain vocabulary.
3. `.guidelines/java.md` and `.guidelines/spring-boot.md` define stack
   conventions.
4. Existing code and tests describe current behavior but do not override an
   approved contract.

Surface conflicts instead of silently selecting a source.

## Repository purpose

Product Catalog is a Spring Boot 3.x learning application that exposes an
in-memory REST API for product management. It demonstrates layered design,
dependency injection, validation, centralized error handling, configuration
profiles, and automated testing. It deliberately has no database, JPA, or Lombok.

## Gated workflow

Use **Specify → Plan → Tasks → Implement**. Stop for human approval after each
phase. Do not implement behavior that is not in the approved spec.

| Phase | Artifact | Global skill | Gate |
| --- | --- | --- | --- |
| Specify | `SPEC.md`, `docs/specs/product-catalog/` | `spec-driven-development` (Phase 1) | Human approves spec |
| Plan | `docs/plans/product-catalog-implementation-plan.md` | `spec-driven-development` (Phase 2) + `planning-and-task-breakdown` | Human approves plan |
| Tasks | `docs/plans/product-catalog-tasks.md` | `spec-driven-development` (Phase 3) + `planning-and-task-breakdown` | Human approves tasks |
| Implement | Source and tests per task | `incremental-implementation` + `test-driven-development` | Human review per task |
| Verify | Test evidence, PR body | run `./mvnw clean verify` before claiming done | Evidence before claims |

**Current status:** Exercises 1–6 implemented on `exercise-6-tests`. New work still
follows the gated workflow and global skills above.

### Project path overrides (addyosmani defaults)

The `spec-driven-development` skill defaults to `tasks/plan.md` and `tasks/todo.md`.
**Override for this repository:**

| Skill default | Use instead |
| --- | --- |
| Spec output | `docs/specs/product-catalog/` chunks indexed by `SPEC.md` |
| `tasks/plan.md` | `docs/plans/product-catalog-implementation-plan.md` |
| `tasks/todo.md` | `docs/plans/product-catalog-tasks.md` |

## Required routing

Use the **bootstrap skills in this repo** (`.agents/skills/*/SKILL.md`). Each
bootstrap tells the agent to fetch the canonical skill from
[addyosmani/agent-skills](https://github.com/addyosmani/agent-skills) via
`npx skills use` or `npx skills add` if not installed locally. See
`.agents/README.md` and `scripts/setup-agent-skills.sh`.

| Situation | Skill(s) | Also read |
| --- | --- | --- |
| New project, feature, or unclear requirements | `spec-driven-development` | `SPEC.md`, affected spec chunk |
| Breaking work into ordered tasks | `planning-and-task-breakdown` | `docs/plans/` |
| Implementation or bug fix (production behavior) | `test-driven-development` | `.guidelines/java.md`, `.guidelines/spring-boot.md` |
| Multi-file change landing incrementally | `incremental-implementation` | approved task from `docs/plans/product-catalog-tasks.md` |
| API, validation, config, or error-handling detail | — | matching `docs/specs/product-catalog/` chunk in `SPEC.md` |
| Domain terminology | — | `CONTEXT.md` |

## Ownership and layout

```text
src/main/java/com/codewalnut/productcatalog/
  controller/     HTTP adapters only
  service/        business rules and orchestration
  repository/     in-memory persistence boundary
  model/          internal domain entities
  dto/            request/response and error payloads
  exception/      domain exceptions and global handler
  config/         @ConfigurationProperties and profile wiring
src/test/java/    mirrors production packages
SPEC.md           product contract index and approval status
docs/specs/product-catalog/   capability contract chunks
docs/plans/       implementation plan and tasks (after spec approval)
CONTEXT.md        stable domain glossary
.agents/skills/   bootstrap SKILL.md → fetch canonical skills from web
.guidelines/      stable stack conventions
```

Create feature packages only when implementing that capability. Do not add empty
placeholder classes.

## Commands

Use the checked-in Maven Wrapper:

```bash
./mvnw clean verify
./mvnw test
./mvnw spring-boot:run
./mvnw -Dtest=ClassName test
```

The project targets Java 21 and Spring Boot 3.4.x. A matching JDK is required.

## Git and pull request conventions

### Commit messages

Use [Conventional Commits](https://www.conventionalcommits.org/) with this shape:

```text
feat(scope): short imperative subject
fix(scope): short imperative subject
docs(scope): short imperative subject
test(scope): short imperative subject
chore(scope): short imperative subject
```

Examples:

- `feat(api): add GET /api/info endpoint`
- `feat(product): implement in-memory product repository`
- `fix(validation): reject duplicate SKU case-insensitively`
- `test(service): cover low-stock filtering with test profile`
- `docs(readme): document available profiles and endpoints`
- `chore(setup): scaffold agentic boilerplate and draft spec`

Rules:

- Use lowercase type and scope.
- Subject is imperative, present tense, no trailing period.
- Body is optional; reference requirement IDs when useful (`REQ-020`).
- One logical change per commit when possible.

### Pull request titles

Match commit style:

```text
feat(scope): short description of the deliverable
```

Examples:

- `feat(setup): agentic boilerplate and draft product catalog spec`
- `feat(exercise-1): project setup and info endpoint`
- `feat(exercise-6): automated tests and actuator configuration`

Branch naming (recommended):

```text
exercise-N-short-description
feat/short-description
```

Use the PR template at `.github/pull_request_template.md`.

## Working boundaries

Always:

- Keep changes scoped to an approved requirement and task.
- Write a failing behavior test before production behavior (TDD).
- Use constructor injection only; never field `@Autowired`.
- Use DTOs at REST boundaries; never expose internal model objects.
- Use `BigDecimal` for price; validate all external inputs.
- Preserve unrelated work and inspect the complete diff before handoff.

Ask first:

- Changing the approved spec or Java/Spring Boot version.
- Adding dependencies beyond the exercise list (web, validation, actuator, test).
- Adding database, JPA, Lombok, or CI workflows.
- Changing public API contracts or package boundaries.

Never:

- Add secrets, credentials, or personal environment data to the repo.
- Log passwords, tokens, or complete sensitive request bodies.
- Use Lombok, Spring Data JPA, or a database for this exercise.
- Weaken, skip, or delete a failing test to obtain a green build.
- Implement behavior that is not in the approved spec.
- Commit, push, or open a pull request without explicit user authorization.
