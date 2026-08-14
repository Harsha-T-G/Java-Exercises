package org.example;

import org.example.productinventory.domain.Product;
import org.example.productinventory.service.Inventory;

public class Main {

    public static void main(String[] args) {
        Inventory inventory = new Inventory();

        inventory.addProduct(new Product("P-101", "Laptop", "Electronics", 8));
        inventory.addProduct(new Product("P-102", "Mouse", "Electronics", 3));
        inventory.addProduct(new Product("P-103", "Notebook", "Stationery", 20));
        inventory.addProduct(new Product("P-104", "Desk Chair", "Furniture", 5));

        inventory.addStock("P-102", 4);
        inventory.removeStock("P-101", 2);

        System.out.println("Electronics products sorted by name:");
        inventory.findByCategory("Electronics").forEach(System.out::println);

        System.out.println("\nProducts with stock below 10:");
        inventory.findLowStockProducts(10).forEach(System.out::println);

        System.out.println("\nTotal stock by category:");
        inventory.getStockByCategory().forEach((category, quantity) ->
                System.out.println(category + ": " + quantity));

        System.out.println("\nCategories: " + inventory.getCategories());
    }
}
