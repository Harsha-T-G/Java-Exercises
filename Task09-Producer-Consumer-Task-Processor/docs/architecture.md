# Architecture and concurrency contract

`service.TaskProcessor` owns a bounded `BlockingQueue<domain.Task>` and a fixed set of consumer threads. Producers may call `submit` concurrently. The application demonstrates two producer threads and three consumers.

## Lifecycle

1. `start()` starts consumers.
2. `submit()` enters the admission read lock, atomically reserves a unique ID, blocks if the bounded queue is full, and records a submission only after insertion succeeds.
3. A consumer removes one task, transitions it from `PENDING` to `PROCESSING`, and invokes the handler.
4. The task transitions once to `COMPLETED` or `FAILED`.
5. `shutdownGracefully()` takes the admission write lock before closing admission. This waits for already-authorized submissions and prevents the shutdown/submit race. Consumers use a timed blocking `poll` and exit only when admission is closed and the queue has drained. The caller then waits for termination.

The timed poll is a blocking operation, not a busy-wait loop. It lets an empty consumer periodically observe shutdown without synthetic queue entries.

## Accounting invariant

At every stable summary point:

`submitted = processed + failed + pending`

Pending includes both queued and currently processing tasks. After normal graceful shutdown, pending is zero.

## Interruption policy

- A producer interrupted while blocked in `put` rolls back its reserved ID and propagates `InterruptedException`.
- A consumer interrupted while waiting restores its interrupt status and exits.
- A consumer interrupted by the task handler marks its already-acquired task failed, restores interrupt status, and exits. The accepted task is therefore never silently lost.
- A caller interrupted while awaiting shutdown receives `InterruptedException`; responsibility for restoring the flag belongs to the boundary that consumes that exception (the demo `main` does so).

This is an in-process guarantee. Crash-safe exactly-once processing would require durable storage and transactional/idempotent processing.
