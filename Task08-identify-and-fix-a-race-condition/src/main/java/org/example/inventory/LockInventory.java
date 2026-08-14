package org.example.inventory;

import java.util.concurrent.locks.ReentrantLock;

/** Protects the check-and-update operation with an explicit lock. */
public final class LockInventory implements StockInventory {
    private final ReentrantLock lock = new ReentrantLock();
    private int stock;

    public LockInventory(int initialStock) {
        if (initialStock < 0) throw new IllegalArgumentException("initialStock must not be negative");
        stock = initialStock;
    }

    @Override
    public boolean reduce(int amount) {
        validateAmount(amount);
        lock.lock();
        try {
            if (stock < amount) return false;
            stock -= amount;
            return true;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int getStock() {
        lock.lock();
        try {
            return stock;
        } finally {
            lock.unlock();
        }
    }

    private static void validateAmount(int amount) {
        if (amount <= 0) throw new IllegalArgumentException("amount must be positive");
    }
}
