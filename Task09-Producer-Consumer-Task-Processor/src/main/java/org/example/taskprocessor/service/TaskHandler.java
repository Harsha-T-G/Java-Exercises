package org.example.taskprocessor.service;

import org.example.taskprocessor.domain.Task;

@FunctionalInterface
public interface TaskHandler {
    void process(Task task) throws Exception;
}
