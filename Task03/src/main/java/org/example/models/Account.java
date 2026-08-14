package org.example.models;

import org.example.constants.AccountStatus;
import org.example.constants.TransactionType;
import org.example.exception.NegativeInitialBalanceException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
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

    public Account(long accountNumber, String accountHolderName, AccountStatus status) {
        this.accountNumber = accountNumber;
        this.accountHolderName = accountHolderName;
        this.currentBalance = BigDecimal.ZERO;
        this.accountStatus = (status != null) ? status : AccountStatus.ACTIVE;
        this.transactionHistory = new ArrayList<>();
    }

    public Account(long accountNumber, String accountHolderName) {
        this(accountNumber, accountHolderName, AccountStatus.ACTIVE);
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

    public void setCurrentBalance(BigDecimal currentBalance) {
        this.currentBalance = currentBalance;
    }

    public AccountStatus getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(AccountStatus accountStatus) {
        this.accountStatus = accountStatus;
    }

    public List<Transaction> getTransactionHistory() {
        return Collections.unmodifiableList(transactionHistory);
    }

    public void addTransaction(Transaction transaction) {
        this.transactionHistory.add(transaction);
    }
}