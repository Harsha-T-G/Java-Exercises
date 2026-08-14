package org.example.productinventory.service;

import org.example.productinventory.domain.Product;
import org.example.productinventory.exception.DuplicateProductException;
import org.example.productinventory.exception.InsufficientStockException;
import org.example.productinventory.exception.InvalidProductException;
import org.example.productinventory.exception.InvalidQuantityException;
import org.example.productinventory.exception.ProductNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InventoryTest {

    private Inventory inventory;

    @BeforeEach
    void setUp() {
        inventory = new Inventory();
    }

    @Test
    void givenValidProduct_whenAdded_thenItCanBeFoundByCategory() {
        Product product = product("P-1", "Laptop", "Electronics", 10);

        inventory.addProduct(product);

        assertEquals(List.of(product), inventory.findByCategory("Electronics"));
    }

    @Test
    void givenExistingProductId_whenAddedAgain_thenRejectsDuplicate() {
        inventory.addProduct(product("P-1", "Laptop", "Electronics", 10));

        assertThrows(DuplicateProductException.class,
                () -> inventory.addProduct(product("P-1", "Notebook", "Stationery", 5)));
    }

    @Test
    void givenInvalidProductData_whenAdded_thenRejectsIt() {
        assertThrows(InvalidProductException.class, () -> inventory.addProduct(null));
        assertThrows(InvalidProductException.class,
                () -> inventory.addProduct(product(" ", "Laptop", "Electronics", 10)));
        assertThrows(InvalidProductException.class,
                () -> inventory.addProduct(product("P-1", null, "Electronics", 10)));
        assertThrows(InvalidProductException.class,
                () -> inventory.addProduct(product("P-1", "Laptop", " ", 10)));
    }

    @Test
    void givenNegativeInitialQuantity_whenProductIsAdded_thenRejectsIt() {
        Product product = product("P-1", "Laptop", "Electronics", -1);

        assertThrows(InvalidProductException.class, () -> inventory.addProduct(product));
    }

    @Test
    void givenExistingProduct_whenStockIsAddedAndRemoved_thenUpdatesQuantity() {
        inventory.addProduct(product("P-1", "Laptop", "Electronics", 10));

        inventory.addStock("P-1", 5);
        inventory.removeStock("P-1", 3);

        assertEquals(12, inventory.findByCategory("Electronics").getFirst().quantity());
    }

    @Test
    void givenZeroOrNegativeQuantity_whenStockIsChanged_thenRejectsIt() {
        inventory.addProduct(product("P-1", "Laptop", "Electronics", 10));

        assertThrows(InvalidQuantityException.class, () -> inventory.addStock("P-1", 0));
        assertThrows(InvalidQuantityException.class, () -> inventory.addStock("P-1", -1));
        assertThrows(InvalidQuantityException.class, () -> inventory.removeStock("P-1", 0));
        assertThrows(InvalidQuantityException.class, () -> inventory.removeStock("P-1", -1));
    }

    @Test
    void givenInsufficientStock_whenStockIsRemoved_thenRejectsItAndKeepsStockUnchanged() {
        inventory.addProduct(product("P-1", "Laptop", "Electronics", 4));

        assertThrows(InsufficientStockException.class,
                () -> inventory.removeStock("P-1", 5));

        assertEquals(4, inventory.findByCategory("Electronics").getFirst().quantity());
    }

    @Test
    void givenMissingProduct_whenStockIsChanged_thenThrowsProductNotFoundException() {
        assertThrows(ProductNotFoundException.class, () -> inventory.addStock("missing", 2));
        assertThrows(ProductNotFoundException.class, () -> inventory.removeStock("missing", 2));
    }

    @Test
    void givenProductsInCategory_whenFound_thenReturnsThemSortedByName() {
        Product mouse = product("P-3", "Mouse", "Electronics", 5);
        Product adapter = product("P-2", "Adapter", "Electronics", 7);
        Product laptop = product("P-1", "Laptop", "Electronics", 3);
        inventory.addProduct(mouse);
        inventory.addProduct(adapter);
        inventory.addProduct(laptop);
        inventory.addProduct(product("P-4", "Notebook", "Stationery", 10));

        List<Product> result = inventory.findByCategory("Electronics");

        assertEquals(List.of(adapter, laptop, mouse), result);
    }

    @Test
    void givenProductsBelowLimit_whenFound_thenSortsByQuantityAndThenName() {
        Product keyboard = product("P-1", "Keyboard", "Electronics", 2);
        Product mouse = product("P-2", "Mouse", "Electronics", 5);
        Product adapter = product("P-3", "Adapter", "Electronics", 5);
        inventory.addProduct(mouse);
        inventory.addProduct(product("P-4", "Monitor", "Electronics", 10));
        inventory.addProduct(keyboard);
        inventory.addProduct(adapter);

        List<Product> result = inventory.findLowStockProducts(10);

        assertEquals(List.of(keyboard, adapter, mouse), result);
    }

    @Test
    void givenProductsInSeveralCategories_whenStockIsGrouped_thenCalculatesTotals() {
        inventory.addProduct(product("P-1", "Laptop", "Electronics", 10));
        inventory.addProduct(product("P-2", "Mouse", "Electronics", 5));
        inventory.addProduct(product("P-3", "Notebook", "Stationery", 20));

        Map<String, Integer> result = inventory.getStockByCategory();

        assertEquals(Map.of("Electronics", 15, "Stationery", 20), result);
    }

    @Test
    void givenRepeatedCategories_whenCategoriesAreRequested_thenReturnsUniqueAlphabeticalValues() {
        inventory.addProduct(product("P-1", "Laptop", "Electronics", 10));
        inventory.addProduct(product("P-2", "Mouse", "Electronics", 5));
        inventory.addProduct(product("P-3", "Notebook", "Stationery", 20));
        inventory.addProduct(product("P-4", "Chair", "Furniture", 4));

        Set<String> categories = inventory.getCategories();

        assertEquals(List.of("Electronics", "Furniture", "Stationery"),
                new ArrayList<>(categories));
    }

    @Test
    void givenResultCollections_whenModificationIsAttempted_thenTheyAreImmutable() {
        inventory.addProduct(product("P-1", "Laptop", "Electronics", 10));

        List<Product> categoryProducts = inventory.findByCategory("Electronics");
        List<Product> lowStockProducts = inventory.findLowStockProducts(20);
        Map<String, Integer> stockByCategory = inventory.getStockByCategory();
        Set<String> categories = inventory.getCategories();

        assertThrows(UnsupportedOperationException.class, categoryProducts::clear);
        assertThrows(UnsupportedOperationException.class, lowStockProducts::clear);
        assertThrows(UnsupportedOperationException.class,
                () -> stockByCategory.put("Stationery", 5));
        assertThrows(UnsupportedOperationException.class, () -> categories.add("Stationery"));
    }

    private static Product product(
            String productId, String name, String category, int quantity) {
        return new Product(productId, name, category, quantity);
    }
}
