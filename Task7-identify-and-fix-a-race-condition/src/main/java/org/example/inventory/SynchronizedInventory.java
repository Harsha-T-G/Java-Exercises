package org.example.inventory;

/** Protects the check-and-update operation with the object's intrinsic lock. */
public final class SynchronizedInventory implements StockInventory {
    private int stock;

    public SynchronizedInventory(int initialStock) {
        if (initialStock < 0) throw new IllegalArgumentException("initialStock must not be negative");
        stock = initialStock;
    }

    @Override
    public synchronized boolean reduce(int amount) {
        validateAmount(amount);
        if (stock < amount) return false;
        stock -= amount;
        return true;
    }

    @Override
    public synchronized int getStock() {
        return stock;
    }

    private static void validateAmount(int amount) {
        if (amount <= 0) throw new IllegalArgumentException("amount must be positive");
    }
}
