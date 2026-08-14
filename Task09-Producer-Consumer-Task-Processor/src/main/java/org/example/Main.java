package org.example;

import org.example.taskprocessor.domain.ProcessingSummary;
import org.example.taskprocessor.domain.Task;
import org.example.taskprocessor.service.TaskProcessor;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public final class Main {
    private static final int PRODUCER_COUNT = 2;
    private static final int TASKS_PER_PRODUCER = 25;

    private Main() { }

    public static void main(String[] args) {
        TaskProcessor processor = new TaskProcessor(10, 3, task ->
                System.out.printf("%s processed %s%n", Thread.currentThread().getName(), task.id()));
        ExecutorService producers = Executors.newFixedThreadPool(PRODUCER_COUNT);

        try {
            processor.start();
            List<Future<?>> producerResults = new ArrayList<>();
            for (int producer = 1; producer <= PRODUCER_COUNT; producer++) {
                int producerId = producer;
                producerResults.add(producers.submit(() -> produce(processor, producerId)));
            }
            for (Future<?> result : producerResults) {
                result.get();
            }
            producers.shutdown();

            ProcessingSummary summary = processor.shutdownGracefully(Duration.ofSeconds(10));
            System.out.printf("Summary: submitted=%d, processed=%d, failed=%d, pending=%d%n",
                    summary.submitted(), summary.processed(), summary.failed(), summary.pending());
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            System.err.println("Application interrupted; shutdown requested.");
        } catch (Exception failure) {
            System.err.println("Task processor failed: " + failure.getMessage());
        } finally {
            producers.shutdownNow();
            processor.close();
        }
    }

    private static void produce(TaskProcessor processor, int producerId) {
        try {
            for (int taskNumber = 1; taskNumber <= TASKS_PER_PRODUCER; taskNumber++) {
                String id = "P" + producerId + "-T" + taskNumber;
                processor.submit(Task.create(id, "Task " + taskNumber + " from producer " + producerId));
            }
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
        }
    }
}
