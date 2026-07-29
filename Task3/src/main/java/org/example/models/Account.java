package org.example.models;

import org.example.constants.AccountStatus;
import org.example.constants.TransactionType;
import org.example.exception.AccountBlockedException;
import org.example.exception.InsufficientFundsException;
import org.example.exception.InvalidAmountException;
import org.example.exception.NegativeInitialBalanceException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Account {

    private final long accountNumber;
    private String accountHolderName;
    private BigDecimal currentBalance;
    private AccountStatus accountStatus;
    private final List<Transaction> transactionHistory;

    public Account(long accountNumber, String accountHolderName, BigDecimal initialBalance, AccountStatus status) {
        if (initialBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new NegativeInitialBalanceException("Initial balance cannot be negative");
        }
        this.accountNumber = accountNumber;
        this.accountHolderName = accountHolderName;
        this.currentBalance = initialBalance;
        this.accountStatus = status;
        this.transactionHistory = new ArrayList<>();

        if (initialBalance.compareTo(BigDecimal.ZERO) > 0) {
            Transaction initialDeposit = new Transaction(
                UUID.randomUUID().toString(),
                TransactionType.DEPOSIT,
                initialBalance,
                BigDecimal.ZERO,
                initialBalance,
                LocalDateTime.now()
            );
            this.transactionHistory.add(initialDeposit);
        }
    }

    public Account(long accountNumber, String accountHolderName) {
        this.accountNumber = accountNumber;
        this.accountHolderName = accountHolderName;
        this.currentBalance = BigDecimal.ZERO;
        this.accountStatus = AccountStatus.ACTIVE;
        this.transactionHistory = new ArrayList<>();
    }

    public long getAccountNumber() {
        return accountNumber;
    }

    public String getAccountHolderName() {
        return accountHolderName;
    }

    public void setAccountHolderName(String accountHolderName) {
        this.accountHolderName = accountHolderName;
    }

    public BigDecimal getCurrentBalance() {
        return currentBalance;
    }

    public AccountStatus getAccountStatus() {
        return accountStatus;
    }

    public List<Transaction> getTransactionHistory() {
        return List.copyOf(transactionHistory);
    }

    public BigDecimal deposit(BigDecimal amount) throws InvalidAmountException, AccountBlockedException {
        if (accountStatus == AccountStatus.BLOCKED) {
            throw new AccountBlockedException("Cannot deposit to blocked account");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Deposit amount must be positive");
        }

        BigDecimal balanceBefore = this.currentBalance;
        this.currentBalance = this.currentBalance.add(amount);

        Transaction transaction = new Transaction(
            UUID.randomUUID().toString(),
            TransactionType.DEPOSIT,
            amount,
            balanceBefore,
            this.currentBalance,
            LocalDateTime.now()
        );
        this.transactionHistory.add(transaction);
        return this.currentBalance;
    }

    public BigDecimal withdraw(BigDecimal amount) throws InsufficientFundsException, InvalidAmountException, AccountBlockedException {
        if (accountStatus == AccountStatus.BLOCKED) {
            throw new AccountBlockedException("Cannot withdraw from blocked account");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Withdrawal amount must be positive");
        }
        if (amount.compareTo(this.currentBalance) > 0) {
            throw new InsufficientFundsException("Insufficient balance for withdrawal");
        }

        BigDecimal balanceBefore = this.currentBalance;
        this.currentBalance = this.currentBalance.subtract(amount);

        Transaction transaction = new Transaction(
            UUID.randomUUID().toString(),
            TransactionType.WITHDRAWAL,
            amount,
            balanceBefore,
            this.currentBalance,
            LocalDateTime.now()
        );
        this.transactionHistory.add(transaction);
        return this.currentBalance;
    }

    public void blockAccount() {
        this.accountStatus = AccountStatus.BLOCKED;
    }

    public void activateAccount() {
        this.accountStatus = AccountStatus.ACTIVE;
    }
}