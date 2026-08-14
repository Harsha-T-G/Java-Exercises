package org.example.taskprocessor.service;

import org.example.taskprocessor.domain.ProcessingSummary;
import org.example.taskprocessor.domain.Task;
import org.example.taskprocessor.domain.TaskStatus;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TaskProcessorTest {

    @Test
    void givenTwoConcurrentProducers_whenFiftyTasksAreSubmitted_thenEveryTaskIsProcessedOnce()
            throws Exception {
        // Given
        Set<String> handlerCalls = ConcurrentHashMap.newKeySet();
        TaskProcessor processor = new TaskProcessor(7, 3, task -> {
            if (!handlerCalls.add(task.id())) {
                throw new AssertionError("duplicate handler call for " + task.id());
            }
        });
        ExecutorService producers = Executors.newFixedThreadPool(2);
        CountDownLatch startTogether = new CountDownLatch(1);
        processor.start();

        try {
            List<Future<?>> producerResults = new ArrayList<>();
            for (int producer = 1; producer <= 2; producer++) {
                int producerId = producer;
                producerResults.add(producers.submit(() -> {
                    startTogether.await();
                    for (int task = 1; task <= 25; task++) {
                        processor.submit(Task.create(producerId + "-" + task, "work"));
                    }
                    return null;
                }));
            }

            // When
            startTogether.countDown();
            for (Future<?> result : producerResults) {
                result.get(5, TimeUnit.SECONDS);
            }
            ProcessingSummary summary = processor.shutdownGracefully(Duration.ofSeconds(5));

            // Then
            assertEquals(new ProcessingSummary(50, 50, 0, 0), summary);
            assertEquals(50, handlerCalls.size());
            assertEquals(handlerCalls, processor.processedTaskIds());
            assertTrue(processor.isTerminated());
        } finally {
            producers.shutdownNow();
            processor.close();
        }
    }

    @Test
    void givenThreeBlockedTasks_whenConsumersStart_thenAllThreeConsumersProcessConcurrently()
            throws Exception {
        // Given: every handler reports its start and waits, so all three must overlap.
        CountDownLatch allConsumersStarted = new CountDownLatch(3);
        CountDownLatch releaseConsumers = new CountDownLatch(1);
        Set<String> consumerNames = ConcurrentHashMap.newKeySet();
        TaskProcessor processor = new TaskProcessor(3, 3, task -> {
            consumerNames.add(Thread.currentThread().getName());
            allConsumersStarted.countDown();
            if (!releaseConsumers.await(2, TimeUnit.SECONDS)) {
                throw new IllegalStateException("consumers were not released");
            }
        });
        processor.start();
        for (int task = 1; task <= 3; task++) {
            processor.submit(Task.create("task-" + task, "work"));
        }

        try {
            // When
            boolean startedConcurrently = allConsumersStarted.await(1, TimeUnit.SECONDS);
            releaseConsumers.countDown();
            ProcessingSummary summary = processor.shutdownGracefully(Duration.ofSeconds(5));

            // Then
            assertTrue(startedConcurrently, "all consumers should start before any is released");
            assertEquals(3, consumerNames.size());
            assertEquals(new ProcessingSummary(3, 3, 0, 0), summary);
        } finally {
            releaseConsumers.countDown();
            processor.close();
        }
    }

    @Test
    void givenFullBoundedQueue_whenBlockedProducerIsInterrupted_thenTaskIsNotSubmitted()
            throws Exception {
        // Given: one task occupies the consumer and one fills the queue.
        CountDownLatch handlerStarted = new CountDownLatch(1);
        CountDownLatch releaseHandler = new CountDownLatch(1);
        TaskProcessor processor = new TaskProcessor(1, 1, task -> {
            handlerStarted.countDown();
            releaseHandler.await();
        });
        processor.start();
        processor.submit(Task.create("running", "work"));
        assertTrue(handlerStarted.await(1, TimeUnit.SECONDS));
        processor.submit(Task.create("queued", "work"));
        CountDownLatch interruptionPreserved = new CountDownLatch(1);
        Thread blockedProducer = new Thread(() -> {
            try {
                processor.submit(Task.create("interrupted", "work"));
            } catch (InterruptedException expected) {
                Thread.currentThread().interrupt();
                if (Thread.currentThread().isInterrupted()) {
                    interruptionPreserved.countDown();
                }
            }
        }, "blocked-producer");

        try {
            // When
            blockedProducer.start();
            blockedProducer.interrupt();
            blockedProducer.join(1_000);

            // Then
            assertFalse(blockedProducer.isAlive());
            assertTrue(interruptionPreserved.await(1, TimeUnit.SECONDS));
            assertEquals(2, processor.summary().submitted());

            releaseHandler.countDown();
            assertEquals(new ProcessingSummary(2, 2, 0, 0),
                    processor.shutdownGracefully(Duration.ofSeconds(5)));
        } finally {
            releaseHandler.countDown();
            processor.close();
        }
    }

    @Test
    void givenDuplicateTaskId_whenSecondTaskIsSubmitted_thenItIsRejectedBeforeProcessing()
            throws Exception {
        // Given
        CountDownLatch releaseHandler = new CountDownLatch(1);
        TaskProcessor processor = new TaskProcessor(2, 1, task -> releaseHandler.await());
        processor.start();
        processor.submit(Task.create("same-id", "first"));

        try {
            // When
            IllegalArgumentException failure = assertThrows(IllegalArgumentException.class,
                    () -> processor.submit(Task.create("same-id", "second")));

            // Then
            assertTrue(failure.getMessage().contains("duplicate task ID"));
            assertEquals(1, processor.summary().submitted());
            releaseHandler.countDown();
            assertEquals(new ProcessingSummary(1, 1, 0, 0),
                    processor.shutdownGracefully(Duration.ofSeconds(5)));
        } finally {
            releaseHandler.countDown();
            processor.close();
        }
    }

    @Test
    void givenHandlerFailures_whenShutdownBegins_thenFailuresAreCountedAndQueueStillDrains()
            throws Exception {
        // Given
        TaskProcessor processor = new TaskProcessor(4, 3, task -> {
            if (task.id().endsWith("5")) {
                throw new IllegalStateException("simulated failure");
            }
        });
        List<Task> tasks = new ArrayList<>();
        processor.start();
        for (int index = 0; index < 20; index++) {
            Task task = Task.create("task-" + index, "work");
            tasks.add(task);
            processor.submit(task);
        }

        try {
            // When
            ProcessingSummary summary = processor.shutdownGracefully(Duration.ofSeconds(5));

            // Then
            assertEquals(new ProcessingSummary(20, 18, 2, 0), summary);
            assertEquals(18, tasks.stream().filter(t -> t.status() == TaskStatus.COMPLETED).count());
            assertEquals(2, tasks.stream().filter(t -> t.status() == TaskStatus.FAILED).count());
            assertTrue(processor.isTerminated());
        } finally {
            processor.close();
        }
    }

    @Test
    void givenHandlerIsInterrupted_whenTaskIsProcessed_thenFailureIsCountedAndStatusIsRestored()
            throws Exception {
        // Given
        AtomicReference<Thread> consumerThread = new AtomicReference<>();
        TaskProcessor processor = new TaskProcessor(1, 1, task -> {
            consumerThread.set(Thread.currentThread());
            throw new InterruptedException("simulated interruption");
        });
        Task task = Task.create("task-1", "work");
        processor.start();
        processor.submit(task);

        try {
            // When
            ProcessingSummary summary = processor.shutdownGracefully(Duration.ofSeconds(5));

            // Then
            assertEquals(new ProcessingSummary(1, 0, 1, 0), summary);
            assertEquals(TaskStatus.FAILED, task.status());
            assertTrue(consumerThread.get().isInterrupted());
            assertTrue(processor.isTerminated());
        } finally {
            processor.close();
        }
    }

    @Test
    void givenGracefulShutdownCompletes_whenNewSubmissionIsAttempted_thenItIsRejected()
            throws Exception {
        // Given
        TaskProcessor processor = new TaskProcessor(2, 1, task -> { });
        processor.start();
        processor.submit(Task.create("accepted", "work"));

        try {
            // When
            ProcessingSummary summary = processor.shutdownGracefully(Duration.ofSeconds(5));

            // Then
            assertEquals(new ProcessingSummary(1, 1, 0, 0), summary);
            assertTrue(processor.isTerminated());
            assertThrows(IllegalStateException.class,
                    () -> processor.submit(Task.create("late", "work")));
        } finally {
            processor.close();
        }
    }
}
