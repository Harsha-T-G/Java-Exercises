package org.example.inventory;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

class UnsafeInventoryTest {
    private static final int INITIAL_STOCK = 1_000;
    private static final int TASK_COUNT = 100;
    private static final int REDUCTIONS_PER_TASK = 5;
    private static final int EXPECTED_STOCK = 500;

    @Test
    void demonstratesLostUpdates() throws Exception {
        boolean raceObserved = false;
        for (int attempt = 0; attempt < 10 && !raceObserved; attempt++) {
            raceObserved = runTasks(new UnsafeInventory(INITIAL_STOCK)) != EXPECTED_STOCK;
        }
        assertNotEquals(false, raceObserved, "The deliberately widened race should lose updates");
    }

    private int runTasks(UnsafeInventory inventory) throws Exception {
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
            return inventory.getStock();
        } finally {
            executor.shutdown();
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        }
    }
}
