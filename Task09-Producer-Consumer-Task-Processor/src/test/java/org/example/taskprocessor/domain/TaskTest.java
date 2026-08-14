package org.example.taskprocessor.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TaskTest {

    @Test
    void givenValidTaskData_whenTaskIsCreated_thenItStartsPending() {
        // Given
        Instant createdAt = Instant.parse("2026-08-13T06:30:00Z");

        // When
        Task task = new Task("task-1", "Generate report", createdAt);

        // Then
        assertEquals("task-1", task.id());
        assertEquals("Generate report", task.description());
        assertEquals(createdAt, task.createdAt());
        assertEquals(TaskStatus.PENDING, task.status());
    }

    @Test
    void givenBlankIdentityOrDescription_whenTaskIsCreated_thenValidationRejectsIt() {
        // Given
        Instant createdAt = Instant.parse("2026-08-13T06:30:00Z");

        // When / Then
        assertThrows(IllegalArgumentException.class, () -> new Task(" ", "work", createdAt));
        assertThrows(IllegalArgumentException.class, () -> new Task("task-1", " ", createdAt));
        assertThrows(NullPointerException.class, () -> new Task("task-1", "work", null));
    }

    @Test
    void givenPendingTask_whenLegalTransitionsRun_thenTerminalStateCannotChangeAgain() {
        // Given
        Task task = Task.create("task-1", "work");

        // When
        boolean processingStarted = task.startProcessing();
        boolean completed = task.complete();
        boolean failedAfterCompletion = task.fail();

        // Then
        assertTrue(processingStarted);
        assertTrue(completed);
        assertFalse(failedAfterCompletion);
        assertEquals(TaskStatus.COMPLETED, task.status());
    }
}
