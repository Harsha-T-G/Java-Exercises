package org.example;

import org.example.inventory.UnsafeInventory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/** Demonstrates lost updates caused by an unsafe check-then-act operation. */
public class Main {
    private static final int INITIAL_STOCK = 1_000;
    private static final int TASK_COUNT = 100;
    private static final int REDUCTIONS_PER_TASK = 5;

    public static void main(String[] args) throws Exception {
        UnsafeInventory inventory = new UnsafeInventory(INITIAL_STOCK);
        ExecutorService executor = Executors.newFixedThreadPool(16);

        try {
            List<Callable<Void>> tasks = new ArrayList<>();
            for (int task = 0; task < TASK_COUNT; task++) {
                tasks.add(() -> {
                    for (int reduction = 0; reduction < REDUCTIONS_PER_TASK; reduction++) {
                        inventory.reduce(1);
                    }
                    return null;
                });
            }

            for (Future<Void> future : executor.invokeAll(tasks)) {
                future.get();
            }
        } finally {
            executor.shutdown();
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        }

        int expected = INITIAL_STOCK - TASK_COUNT * REDUCTIONS_PER_TASK;
        System.out.printf("Expected stock: %d, actual stock: %d%n", expected, inventory.getStock());
        System.out.println("A mismatch demonstrates lost updates in the unsafe implementation.");
    }
}
