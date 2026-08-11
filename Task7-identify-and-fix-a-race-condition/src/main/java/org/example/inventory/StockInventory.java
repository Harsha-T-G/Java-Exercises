package org.example.inventory;

/** Common contract used to compare thread-safe inventory implementations. */
public interface StockInventory {
    /** Reduces stock when enough is available and reports whether it succeeded. */
    boolean reduce(int amount);

    int getStock();
}
