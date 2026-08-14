# Producer-Consumer Task Processor

A Java 17 exercise demonstrating a bounded `BlockingQueue`, two concurrent producers, three
consumers, exactly-once in-memory processing, explicit task states, interruption handling, and
graceful queue-draining shutdown.

## Project structure

```text
src/main/java/org/example
├── Main.java                                  application entry point and two producers
└── taskprocessor
    ├── domain                                 task state and summary values
    └── service                                queue and consumer orchestration

src/test/java/org/example/taskprocessor
├── domain                                     task model tests
└── service                                    behavior and concurrency tests
```

The project intentionally has no controller, repository, DTO, or configuration layer because it
has no HTTP, persistence, or framework concern. It also intentionally excludes spec-driven
development files and GitHub workflows, as required for this exercise.

## Run

```bash
mvn verify
java -cp target/classes org.example.Main
```

## Test evidence

The JUnit suite directly covers:

- 50 tasks submitted concurrently by two producers and processed exactly once;
- three consumers actually processing concurrently;
- bounded-queue backpressure and interruption of a blocked producer;
- duplicate task-ID rejection;
- handler failures with continued queue draining;
- consumer interruption accounted for as failure with interrupt status restored;
- graceful shutdown, rejected post-shutdown submission, and terminated consumers;
- task input validation and legal status transitions.

See [docs/architecture.md](docs/architecture.md) for lifecycle and accounting invariants.
