package org.example;

import org.example.inventory.AtomicInventory;
import org.example.inventory.LockInventory;
import org.example.inventory.StockInventory;
import org.example.inventory.SynchronizedInventory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/** Compares three correct ways to protect a shared stock counter. */
public class Main {
    private static final int INITIAL_STOCK = 1_000;
    private static final int TASK_COUNT = 100;
    private static final int REDUCTIONS_PER_TASK = 5;

    public static void main(String[] args) throws Exception {
        run("synchronized", new SynchronizedInventory(INITIAL_STOCK));
        run("ReentrantLock", new LockInventory(INITIAL_STOCK));
        run("AtomicInteger (selected)", new AtomicInventory(INITIAL_STOCK));
    }

    private static void run(String name, StockInventory inventory) throws Exception {
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
        System.out.printf("%-26s expected=%d, actual=%d%n", name, expected, inventory.getStock());
    }
}
