package org.example.services;

import org.example.exception.*;
import org.example.models.Account;
import org.example.models.Transaction;
import org.example.constants.AccountStatus;

import java.math.BigDecimal;
import java.util.List;

public class BankingOperations {

    private final AccountServices accountServices = new AccountServices();

    public String createAccount(long accountNumber, String accountHolderName, BigDecimal initialBalance) {
        try {
            if (initialBalance == null) {
                accountServices.createAccount(accountNumber, accountHolderName);
                return "Account created successfully with zero initial balance.";
            } else {
                accountServices.createAccount(accountNumber, accountHolderName, initialBalance);
                return "Account created successfully with initial balance of " + initialBalance;
            }
        } catch (DuplicateAccountNumberException e) {
            return "Error: Account number already exists: " + accountNumber;
        } catch (NegativeInitialBalanceException e) {
            return "Error: Initial balance cannot be negative";
        } catch (IllegalArgumentException e) {
            return "Error: " + e.getMessage();
        }
    }

    public String deposit(long accountNumber, BigDecimal amount) {
        try {
            BigDecimal newBalance = accountServices.deposit(accountNumber, amount);
            return "Amount deposited successfully. New balance: " + newBalance;
        } catch (InvalidAmountException e) {
            return "Error: Deposit amount must be positive";
        } catch (AccountBlockedException e) {
            return "Error: Cannot deposit to blocked account";
        } catch (IllegalArgumentException e) {
            return "Error: Account not found: " + accountNumber;
        }
    }

    public String withdraw(long accountNumber, BigDecimal amount) {
        try {
            BigDecimal newBalance = accountServices.withdraw(accountNumber, amount);
            return "Amount withdrawn successfully. New balance: " + newBalance;
        } catch (InvalidAmountException e) {
            return "Error: Withdrawal amount must be positive";
        } catch (InsufficientFundsException e) {
            return "Error: Insufficient balance for withdrawal";
        } catch (AccountBlockedException e) {
            return "Error: Cannot withdraw from blocked account";
        } catch (IllegalArgumentException e) {
            return "Error: Account not found: " + accountNumber;
        }
    }

    public String blockAccount(long accountNumber) {
        try {
            accountServices.blockAccount(accountNumber);
            return "Account successfully blocked.";
        } catch (IllegalArgumentException e) {
            return "Error: Account not found: " + accountNumber;
        } catch (AccountBlockedException e) {
            return "Error: Account is already blocked";
        }
    }

    public String activateAccount(long accountNumber) {
        try {
            accountServices.activateAccount(accountNumber);
            return "Account successfully activated.";
        } catch (IllegalArgumentException e) {
            if (e.getMessage().equals("Account not found: ")) {
                return "Error: Account not found: ";
            } else if (e.getMessage().equals("Account is already active")) {
                return "Error: Account is already active";
            } else {
                return "Error: " + e.getMessage();
            }
        }
    }

    public String checkBalance(long accountNumber) {
        try {
            BigDecimal balance = accountServices.getBalance(accountNumber);
            return "Current balance: " + balance;
        } catch (IllegalArgumentException e) {
            return "Error: Account not found: " + accountNumber;
        }
    }

    public String getTransactionHistory(long accountNumber) {
        try {
            Account account = accountServices.getAccountDetails(accountNumber);
            java.util.List<Transaction> history = account.getTransactionHistory();
            if (history.isEmpty()) {
                return "No transaction history found for account " + accountNumber +
                       " (Holder: " + account.getAccountHolderName() + ", Status: " + account.getAccountStatus() + ")";
            }
            StringBuilder sb = new StringBuilder();
            sb.append("Account Details:\n");
            sb.append("  Account Number: ").append(accountNumber).append("\n");
            sb.append("  Account Holder: ").append(account.getAccountHolderName()).append("\n");
            sb.append("  Account Status: ").append(account.getAccountStatus()).append("\n");
            sb.append("  Current Balance: ").append(account.getCurrentBalance()).append("\n");
            sb.append("\nTransaction History:\n");
            for (Transaction tx : history) {
                sb.append(String.format("  ID: %s, Type: %s, Amount: %s, Balance Before: %s, Balance After: %s, Timestamp: %s%n",
                        tx.TransactionID(), tx.type(), tx.amount(), tx.balanceBefore(), tx.balanceAfter(), tx.timestamp()));
            }
            return sb.toString();
        } catch (IllegalArgumentException e) {
            return "Error: Account not found: " + accountNumber;
        }
    }

    public String getAccountHolderName(long accountNumber) {
        try {
            Account account = accountServices.getAccountDetails(accountNumber);
            return "Account holder: " + account.getAccountHolderName();
        } catch (IllegalArgumentException e) {
            return "Error: Account not found: " + accountNumber;
        }
    }

    public String getAccountStatus(long accountNumber) {
        try {
            AccountStatus status = accountServices.getAccountStatus(accountNumber);
            return "Account status: " + status;
        } catch (IllegalArgumentException e) {
            return "Error: Account not found: " + accountNumber;
        }
    }
}