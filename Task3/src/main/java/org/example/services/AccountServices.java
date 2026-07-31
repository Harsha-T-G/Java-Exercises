package org.example.services;

import org.example.exception.*;
import org.example.models.Account;
import org.example.models.Transaction;
import org.example.constants.AccountStatus;
import org.example.constants.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AccountServices {

    private final Map<Long, Account> accounts;

    public AccountServices() {
        this.accounts = new HashMap<>();
    }

    public void createAccount(long accountNumber, String accountHolderName, BigDecimal initialBalance)
            throws DuplicateAccountNumberException, NegativeInitialBalanceException {
        if (accounts.containsKey(accountNumber)) {
            throw new DuplicateAccountNumberException("Account number already exists: " + accountNumber);
        }

        Account account = new Account(accountNumber, accountHolderName, initialBalance, AccountStatus.ACTIVE);
        accounts.put(accountNumber, account);
    }

    public void createAccount(long accountNumber, String accountHolderName)
            throws DuplicateAccountNumberException {
        if (accounts.containsKey(accountNumber)) {
            throw new DuplicateAccountNumberException("Account number already exists: " + accountNumber);
        }

        Account account = new Account(accountNumber, accountHolderName);
        accounts.put(accountNumber, account);
    }

    public BigDecimal deposit(long accountNumber, BigDecimal amount)
            throws InvalidAmountException, AccountBlockedException {
        Account account = accounts.get(accountNumber);
        if (account == null) {
            throw new IllegalArgumentException("Account not found: " + accountNumber);
        }
        if (account.getAccountStatus() == AccountStatus.BLOCKED) {
            throw new AccountBlockedException("Cannot deposit to blocked account");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Deposit amount must be positive");
        }

        BigDecimal balanceBefore = account.getCurrentBalance();
        BigDecimal balanceAfter = balanceBefore.add(amount);
        account.setCurrentBalance(balanceAfter);

        Transaction transaction = new Transaction(
            UUID.randomUUID().toString(),
            TransactionType.DEPOSIT,
            amount,
            balanceBefore,
            balanceAfter,
            LocalDateTime.now()
        );
        account.addTransaction(transaction);

        return account.getCurrentBalance();
    }

    public BigDecimal withdraw(long accountNumber, BigDecimal amount)
            throws InsufficientFundsException, InvalidAmountException, AccountBlockedException {
        Account account = accounts.get(accountNumber);
        if (account == null) {
            throw new IllegalArgumentException("Account not found: " + accountNumber);
        }
        if (account.getAccountStatus() == AccountStatus.BLOCKED) {
            throw new AccountBlockedException("Cannot withdraw from blocked account");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Withdrawal amount must be positive");
        }
        if (amount.compareTo(account.getCurrentBalance()) > 0) {
            throw new InsufficientFundsException("Insufficient balance for withdrawal");
        }

        BigDecimal balanceBefore = account.getCurrentBalance();
        BigDecimal balanceAfter = balanceBefore.subtract(amount);
        account.setCurrentBalance(balanceAfter);

        Transaction transaction = new Transaction(
            UUID.randomUUID().toString(),
            TransactionType.WITHDRAWAL,
            amount,
            balanceBefore,
            balanceAfter,
            LocalDateTime.now()
        );
        account.addTransaction(transaction);

        return account.getCurrentBalance();
    }

    public void blockAccount(long accountNumber) {
        Account account = accounts.get(accountNumber);
        if (account == null) {
            throw new IllegalArgumentException("Account not found: " + accountNumber);
        }
        if (account.getAccountStatus() == AccountStatus.BLOCKED) {
            throw new AccountBlockedException("Account is already blocked");
        }
        account.setAccountStatus(AccountStatus.BLOCKED);
    }

    public void activateAccount(long accountNumber) {
        Account account = accounts.get(accountNumber);
        if (account == null) {
            throw new IllegalArgumentException("Account not found: " + accountNumber);
        }
        if (account.getAccountStatus() == AccountStatus.ACTIVE) {
            throw new IllegalArgumentException("Account is already active");
        }
        account.setAccountStatus(AccountStatus.ACTIVE);
    }

    public BigDecimal getBalance(long accountNumber) {
        Account account = accounts.get(accountNumber);
        if (account == null) {
            throw new IllegalArgumentException("Account not found: " + accountNumber);
        }
        return account.getCurrentBalance();
    }

    public Account getAccountDetails(long accountNumber) {
        Account account = accounts.get(accountNumber);
        if (account == null) {
            throw new IllegalArgumentException("Account not found: " + accountNumber);
        }
        return account;
    }

    public boolean accountExists(long accountNumber) {
        return accounts.containsKey(accountNumber);
    }

    public AccountStatus getAccountStatus(long accountNumber) {
        Account account = accounts.get(accountNumber);
        if (account == null) {
            throw new IllegalArgumentException("Account not found: " + accountNumber);
        }
        return account.getAccountStatus();
    }
}