package org.example.inventory;

/**
 * Unsafe inventory component with no synchronization.
 * Multiple threads can decrement stock without synchronization,
 * leading to race conditions and negative stock.
 */
public class UnsafeInventory {
    private int stock;

    public UnsafeInventory(int initialStock) {
        this.stock = initialStock;
    }

    public void reduce(int amount) {
        int current = stock;
        int next = current - amount;
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        stock = next;
    }

    public int getStock() {
        return stock;
    }
}
