package org.example.services;

import org.example.exception.*;
import org.example.models.Account;
import org.example.constants.AccountStatus;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class AccountServices {

     Map<Long, Account> accounts;

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
        return account.deposit(amount);
    }

    public BigDecimal withdraw(long accountNumber, BigDecimal amount)
            throws InsufficientFundsException, InvalidAmountException, AccountBlockedException {
        Account account = accounts.get(accountNumber);
        if (account == null) {
            throw new IllegalArgumentException("Account not found: " + accountNumber);
        }
        return account.withdraw(amount);
    }

    public void blockAccount(long accountNumber) {
        Account account = accounts.get(accountNumber);
        if (account == null) {
            throw new IllegalArgumentException("Account not found: " + accountNumber);
        }
        account.blockAccount();
    }

    public void activateAccount(long accountNumber) {
        Account account = accounts.get(accountNumber);
        if (account == null) {
            throw new IllegalArgumentException("Account not found: " + accountNumber);
        }
        account.activateAccount();
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