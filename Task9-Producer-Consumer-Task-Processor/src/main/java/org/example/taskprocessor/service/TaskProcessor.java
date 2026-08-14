package org.example.taskprocessor.service;

import org.example.taskprocessor.domain.ProcessingSummary;
import org.example.taskprocessor.domain.Task;
import org.example.taskprocessor.domain.TaskStatus;

import java.time.Duration;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public final class TaskProcessor implements AutoCloseable {
    private static final long SHUTDOWN_CHECK_MILLIS = 100;

    private final BlockingQueue<Task> queue;
    private final TaskHandler handler;
    private final ExecutorService consumers;
    private final Set<String> submittedIds = ConcurrentHashMap.newKeySet();
    private final Set<String> processedIds = ConcurrentHashMap.newKeySet();
    private final AtomicInteger submitted = new AtomicInteger();
    private final AtomicInteger processed = new AtomicInteger();
    private final AtomicInteger failed = new AtomicInteger();
    private final AtomicBoolean started = new AtomicBoolean();
    private final AtomicBoolean accepting = new AtomicBoolean(true);
    private final ReentrantReadWriteLock admissionLock = new ReentrantReadWriteLock(true);
    private final int consumerCount;

    public TaskProcessor(int queueCapacity, int consumerCount, TaskHandler handler) {
        if (queueCapacity <= 0 || consumerCount <= 0) {
            throw new IllegalArgumentException("queueCapacity and consumerCount must be positive");
        }
        this.queue = new ArrayBlockingQueue<>(queueCapacity);
        this.consumerCount = consumerCount;
        this.handler = Objects.requireNonNull(handler, "handler");
        AtomicInteger sequence = new AtomicInteger();
        this.consumers = Executors.newFixedThreadPool(consumerCount, runnable -> {
            Thread thread = new Thread(runnable, "task-consumer-" + sequence.incrementAndGet());
            thread.setDaemon(false);
            return thread;
        });
    }

    public void start() {
        if (!started.compareAndSet(false, true)) {
            throw new IllegalStateException("processor already started");
        }
        for (int index = 0; index < consumerCount; index++) {
            consumers.submit(this::consume);
        }
    }

    public void submit(Task task) throws InterruptedException {
        Objects.requireNonNull(task, "task");
        admissionLock.readLock().lockInterruptibly();
        try {
            if (!started.get()) {
                throw new IllegalStateException("processor has not started");
            }
            if (!accepting.get()) {
                throw new IllegalStateException("processor is shutting down");
            }
            if (task.status() != TaskStatus.PENDING) {
                throw new IllegalArgumentException("only pending tasks may be submitted");
            }
            if (!submittedIds.add(task.id())) {
                throw new IllegalArgumentException("duplicate task ID: " + task.id());
            }

            boolean inserted = false;
            try {
                queue.put(task);
                inserted = true;
                submitted.incrementAndGet();
            } finally {
                if (!inserted) {
                    submittedIds.remove(task.id());
                }
            }
        } finally {
            admissionLock.readLock().unlock();
        }
    }

    public ProcessingSummary shutdownGracefully(Duration timeout)
            throws InterruptedException, TimeoutException {
        Objects.requireNonNull(timeout, "timeout");
        if (timeout.isNegative() || timeout.isZero()) {
            throw new IllegalArgumentException("timeout must be positive");
        }
        admissionLock.writeLock().lockInterruptibly();
        try {
            accepting.set(false);
        } finally {
            admissionLock.writeLock().unlock();
        }
        consumers.shutdown();
        if (!consumers.awaitTermination(timeout.toMillis(), TimeUnit.MILLISECONDS)) {
            throw new TimeoutException("consumers did not terminate within " + timeout);
        }
        return summary();
    }

    public ProcessingSummary summary() {
        int submittedCount = submitted.get();
        int processedCount = processed.get();
        int failedCount = failed.get();
        return new ProcessingSummary(
                submittedCount,
                processedCount,
                failedCount,
                submittedCount - processedCount - failedCount);
    }

    public Set<String> processedTaskIds() {
        return Set.copyOf(processedIds);
    }

    public boolean isTerminated() {
        return consumers.isTerminated();
    }

    private void consume() {
        while (accepting.get() || !queue.isEmpty()) {
            try {
                Task task = queue.poll(SHUTDOWN_CHECK_MILLIS, TimeUnit.MILLISECONDS);
                if (task != null) {
                    process(task);
                }
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    private void process(Task task) {
        if (!task.startProcessing()) {
            markFailed(task);
            return;
        }
        try {
            handler.process(task);
            if (!processedIds.add(task.id())) {
                throw new IllegalStateException("task processed more than once: " + task.id());
            }
            if (!task.complete()) {
                throw new IllegalStateException("task could not complete: " + task.id());
            }
            processed.incrementAndGet();
        } catch (InterruptedException interrupted) {
            markFailed(task);
            Thread.currentThread().interrupt();
        } catch (Exception failure) {
            markFailed(task);
        }
    }

    private void markFailed(Task task) {
        if (task.fail()) {
            failed.incrementAndGet();
        }
    }

    @Override
    public void close() {
        admissionLock.writeLock().lock();
        try {
            accepting.set(false);
        } finally {
            admissionLock.writeLock().unlock();
        }
        consumers.shutdown();
    }
}
