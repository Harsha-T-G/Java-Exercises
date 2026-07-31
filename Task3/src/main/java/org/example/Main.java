package org.example;

import org.example.services.BankingOperations;
import java.math.BigDecimal;
import java.util.Scanner;
public class Main {

    private static final BankingOperations bankingOperations = new BankingOperations();
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        boolean exit = false;
        while (!exit) {
            displayMenu();
            int choice = scanner.nextInt();
            scanner.nextLine(); // consume newline
            switch (choice) {
                case 1 -> createAccount();
                case 2 -> depositMoney();
                case 3 -> withdrawMoney();
                case 4 -> blockAccount();
                case 5 -> activateAccount();
                case 6 -> checkBalance();
                case 7 -> viewTransactionHistory();
                case 8 -> {
                    exit = true;
                    System.out.println("Transaction Ended! Thank You For Using the Bank Account");
                }
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }
        scanner.close();
    }

    private static void displayMenu() {
        StringBuilder menu = new StringBuilder();
        menu.append("Please Select the Option\n")
            .append("1. Create Account\n")
            .append("2. Deposit Amount\n")
            .append("3. Withdrawal Amount\n")
            .append("4. Block Account\n")
            .append("5. Activate Account\n")
            .append("6. Check Current Balance\n")
            .append("7. View Transaction History\n")
            .append("8. Exit\n");
        System.out.print(menu.toString());
    }

    private static void createAccount() {
        System.out.println("Enter Account Number, Account Holder Name, Initial Balance (or 0 for no initial balance):");
        long accNumber = scanner.nextLong();
        scanner.nextLine(); // consume newline
        String accName = scanner.nextLine();
        BigDecimal initBalance = scanner.nextBigDecimal();
        scanner.nextLine(); // consume newline

        String result;
        if (initBalance.compareTo(BigDecimal.ZERO) == 0) {
            result = bankingOperations.createAccount(accNumber, accName, null);
        } else {
            result = bankingOperations.createAccount(accNumber, accName, initBalance);
        }
        System.out.println(result);
    }

    private static void depositMoney() {
        System.out.println("Enter Account Number and Deposit Amount:");
        long accNumber = scanner.nextLong();
        scanner.nextLine();
        BigDecimal depositAmt = scanner.nextBigDecimal();
        scanner.nextLine();

        String result = bankingOperations.deposit(accNumber, depositAmt);
        System.out.println(result);
    }

    private static void withdrawMoney() {
        System.out.println("Enter Account Number and Withdrawal Amount:");
        long accNumber = scanner.nextLong();
        scanner.nextLine();
        BigDecimal withdrawalAmt = scanner.nextBigDecimal();
        scanner.nextLine();

        String result = bankingOperations.withdraw(accNumber, withdrawalAmt);
        System.out.println(result);
    }

    private static void blockAccount() {
        System.out.println("Enter Account Number:");
        long accNumber = scanner.nextLong();
        scanner.nextLine();

        String result = bankingOperations.blockAccount(accNumber);
        System.out.println(result);
    }

    private static void activateAccount() {
        System.out.println("Enter Account Number:");
        long accNumber = scanner.nextLong();
        scanner.nextLine();

        String result = bankingOperations.activateAccount(accNumber);
        System.out.println(result);
    }

    private static void checkBalance() {
        System.out.println("Enter Account Number:");
        long accNumber = scanner.nextLong();
        scanner.nextLine();

        String result = bankingOperations.checkBalance(accNumber);
        System.out.println(result);
    }

    private static void viewTransactionHistory() {
        System.out.println("Enter Account Number:");
        long accNumber = scanner.nextLong();
        scanner.nextLine();

        String result = bankingOperations.getTransactionHistory(accNumber);
        System.out.println(result);
    }
}