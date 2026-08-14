# Exercise 7: Identify and Fix a Race Condition

`main` contains the deliberately unsafe implementation. It performs an unprotected
read/modify/write, and the test demonstrates lost updates with 100 concurrent tasks.

`fix/race-condition` contains three safe alternatives:

| Approach | Advantages | Trade-off |
|---|---|---|
| `synchronized` | Smallest, clearest lock-based solution | Intrinsic lock offers little control |
| `ReentrantLock` | Supports timeouts, interruption, fairness and multiple conditions | More code; unlocking must be done in `finally` |
| `AtomicInteger` | Lock-free and concise for a single counter | CAS loops become harder for multi-field invariants |

## Selected solution

`AtomicInteger` is selected because this exercise protects one integer and needs one
conditional update. `compareAndSet` makes the availability check and reduction atomic,
prevents negative stock, and avoids lock-management boilerplate. For a richer inventory
operation spanning several fields, a lock would usually be easier to reason about.

Run the demonstration and tests with:

```shell
mvn compile exec:java -Dexec.mainClass=org.example.Main
mvn test
```
