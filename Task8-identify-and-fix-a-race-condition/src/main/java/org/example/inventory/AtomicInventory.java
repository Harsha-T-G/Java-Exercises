package org.example.inventory;

import java.util.concurrent.atomic.AtomicInteger;

/** Uses compare-and-set so checking and reducing stock is one atomic operation. */
public final class AtomicInventory implements StockInventory {
    private final AtomicInteger stock;

    public AtomicInventory(int initialStock) {
        if (initialStock < 0) throw new IllegalArgumentException("initialStock must not be negative");
        stock = new AtomicInteger(initialStock);
    }

    @Override
    public boolean reduce(int amount) {
        if (amount <= 0) throw new IllegalArgumentException("amount must be positive");
        while (true) {
            int current = stock.get();
            if (current < amount) return false;
            if (stock.compareAndSet(current, current - amount)) return true;
        }
    }

    @Override
    public int getStock() {
        return stock.get();
    }
}
