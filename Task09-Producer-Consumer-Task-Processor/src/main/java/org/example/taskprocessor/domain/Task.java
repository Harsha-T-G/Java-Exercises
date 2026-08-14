package org.example.taskprocessor.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public final class Task {
    private final String id;
    private final String description;
    private final Instant createdAt;
    private final AtomicReference<TaskStatus> status = new AtomicReference<>(TaskStatus.PENDING);

    public Task(String id, String description, Instant createdAt) {
        this.id = requireText(id, "id");
        this.description = requireText(description, "description");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
    }

    public static Task create(String id, String description) {
        return new Task(id, description, Instant.now());
    }

    public String id() { return id; }
    public String description() { return description; }
    public Instant createdAt() { return createdAt; }
    public TaskStatus status() { return status.get(); }

    public boolean startProcessing() {
        return status.compareAndSet(TaskStatus.PENDING, TaskStatus.PROCESSING);
    }

    public boolean complete() {
        return status.compareAndSet(TaskStatus.PROCESSING, TaskStatus.COMPLETED);
    }

    public boolean fail() {
        return status.compareAndSet(TaskStatus.PROCESSING, TaskStatus.FAILED)
                || status.compareAndSet(TaskStatus.PENDING, TaskStatus.FAILED);
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
