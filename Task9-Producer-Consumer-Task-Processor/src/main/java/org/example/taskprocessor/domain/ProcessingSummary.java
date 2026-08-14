package org.example.taskprocessor.domain;

public record ProcessingSummary(int submitted, int processed, int failed, int pending) {
}
