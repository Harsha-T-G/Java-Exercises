package org.example.inventory;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntFunction;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ThreadSafeInventoryTest {
    private static final int TASK_COUNT = 100;
    private static final int REDUCTIONS_PER_TASK = 5;

    static Stream<IntFunction<StockInventory>> implementations() {
        return Stream.of(SynchronizedInventory::new, LockInventory::new, AtomicInventory::new);
    }

    @ParameterizedTest
    @MethodSource("implementations")
    void finalStockIsCorrect(IntFunction<StockInventory> factory) throws Exception {
        StockInventory inventory = factory.apply(1_000);
        RunResult result = runConcurrently(inventory);

        assertEquals(500, result.successfulReductions());
        assertEquals(500, inventory.getStock());
        assertTrue(result.executorTerminated());
    }

    @ParameterizedTest
    @MethodSource("implementations")
    void stockCannotBecomeNegative(IntFunction<StockInventory> factory) throws Exception {
        StockInventory inventory = factory.apply(100);
        RunResult result = runConcurrently(inventory);

        assertEquals(100, result.successfulReductions());
        assertEquals(0, inventory.getStock());
        assertTrue(result.executorTerminated());
    }

    @RepeatedTest(10)
    void selectedAtomicImplementationRemainsCorrectAcrossRepeatedRuns() throws Exception {
        AtomicInventory inventory = new AtomicInventory(1_000);
        RunResult result = runConcurrently(inventory);

        assertEquals(500, result.successfulReductions());
        assertEquals(500, inventory.getStock());
        assertTrue(result.executorTerminated());
    }

    private RunResult runConcurrently(StockInventory inventory) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(16);
        AtomicInteger successes = new AtomicInteger();
        boolean terminated;
        try {
            List<Callable<Void>> tasks = new ArrayList<>();
            for (int task = 0; task < TASK_COUNT; task++) {
                tasks.add(() -> {
                    for (int reduction = 0; reduction < REDUCTIONS_PER_TASK; reduction++) {
                        if (inventory.reduce(1)) successes.incrementAndGet();
                    }
                    return null;
                });
            }
            for (Future<Void> future : executor.invokeAll(tasks)) {
                future.get();
            }
        } finally {
            executor.shutdown();
            terminated = executor.awaitTermination(10, TimeUnit.SECONDS);
            if (!terminated) executor.shutdownNow();
        }
        return new RunResult(successes.get(), terminated);
    }

    private record RunResult(int successfulReductions, boolean executorTerminated) {}
}
