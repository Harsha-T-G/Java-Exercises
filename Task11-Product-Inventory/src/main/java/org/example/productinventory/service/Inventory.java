package org.example.productinventory.service;

import org.example.productinventory.domain.Product;
import org.example.productinventory.exception.DuplicateProductException;
import org.example.productinventory.exception.InsufficientStockException;
import org.example.productinventory.exception.InvalidProductException;
import org.example.productinventory.exception.InvalidQuantityException;
import org.example.productinventory.exception.ProductNotFoundException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class Inventory {

    private static final Comparator<Product> BY_QUANTITY_THEN_NAME =
            Comparator.comparingInt(Product::quantity).thenComparing(Product::name);

    private final Map<String, Product> products = new HashMap<>();

    public void addProduct(Product product) {
        validateProduct(product);
        if (products.containsKey(product.productId())) {
            throw new DuplicateProductException(
                    "Product ID already exists: " + product.productId());
        }
        products.put(product.productId(), product);
    }

    public void addStock(String productId, int quantity) {
        validateStockChange(quantity);
        Product product = findProduct(productId);
        if (quantity > Integer.MAX_VALUE - product.quantity()) {
            throw new InvalidQuantityException("Resulting stock exceeds the supported maximum");
        }

        replaceQuantity(product, product.quantity() + quantity);
    }

    public void removeStock(String productId, int quantity) {
        validateStockChange(quantity);
        Product product = findProduct(productId);
        if (quantity > product.quantity()) {
            throw new InsufficientStockException(
                    "Requested quantity exceeds available stock for product: " + productId);
        }

        replaceQuantity(product, product.quantity() - quantity);
    }

    public List<Product> findByCategory(String category) {
        List<Product> matches = new ArrayList<>();
        if (category == null) {
            return List.copyOf(matches);
        }

        for (Product product : products.values()) {
            if (product.category().equals(category)) {
                matches.add(product);
            }
        }
        matches.sort(Comparator.comparing(Product::name));
        return List.copyOf(matches);
    }

    public List<Product> findLowStockProducts(int limit) {
        List<Product> lowStockProducts = new ArrayList<>();
        for (Product product : products.values()) {
            if (product.quantity() < limit) {
                lowStockProducts.add(product);
            }
        }
        lowStockProducts.sort(BY_QUANTITY_THEN_NAME);
        return List.copyOf(lowStockProducts);
    }

    public Map<String, Integer> getStockByCategory() {
        Map<String, Integer> stockByCategory = new HashMap<>();
        for (Product product : products.values()) {
            stockByCategory.merge(product.category(), product.quantity(), Integer::sum);
        }
        return Map.copyOf(stockByCategory);
    }

    public Set<String> getCategories() {
        Set<String> categories = new TreeSet<>();
        for (Product product : products.values()) {
            categories.add(product.category());
        }
        return Collections.unmodifiableSet(categories);
    }

    private Product findProduct(String productId) {
        Product product = products.get(productId);
        if (product == null) {
            throw new ProductNotFoundException("Product not found: " + productId);
        }
        return product;
    }

    private void replaceQuantity(Product product, int quantity) {
        products.put(product.productId(), new Product(
                product.productId(), product.name(), product.category(), quantity));
    }

    private static void validateProduct(Product product) {
        if (product == null) {
            throw new InvalidProductException("Product must not be null");
        }
        if (isBlank(product.productId())) {
            throw new InvalidProductException("Product ID must not be null or blank");
        }
        if (isBlank(product.name())) {
            throw new InvalidProductException("Product name must not be null or blank");
        }
        if (isBlank(product.category())) {
            throw new InvalidProductException("Product category must not be null or blank");
        }
        if (product.quantity() < 0) {
            throw new InvalidProductException("Initial quantity must not be negative");
        }
    }

    private static void validateStockChange(int quantity) {
        if (quantity <= 0) {
            throw new InvalidQuantityException("Quantity must be greater than zero");
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
