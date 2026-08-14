package org.example.batchtransactionprocessor.process;

import org.example.batchtransactionprocessor.constants.TransactionType;
import org.example.batchtransactionprocessor.domain.BatchResult;
import org.example.batchtransactionprocessor.domain.Transaction;
import org.example.batchtransactionprocessor.domain.TransactionFailure;
import org.example.batchtransactionprocessor.exception.AccountNotFoundException;
import org.example.batchtransactionprocessor.exception.DuplicateTransactionException;
import org.example.batchtransactionprocessor.exception.InsufficientBalanceException;
import org.example.batchtransactionprocessor.exception.InvalidTransactionException;
import org.example.batchtransactionprocessor.exception.TransactionProcessingException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class TransactionProcessor {

    private final LinkedHashMap<String, BigDecimal> accountBalances;

    public TransactionProcessor(Map<String, BigDecimal> initialAccountBalances) {
        Objects.requireNonNull(initialAccountBalances,
                "Initial account balances must not be null");

        this.accountBalances = new LinkedHashMap<>();
        initialAccountBalances.forEach((accountId, balance) -> {
            if (accountId == null || accountId.isBlank()) {
                throw new IllegalArgumentException(
                        "Initial account ID must not be null or blank");
            }
            if (balance == null || balance.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException(
                        "Initial balance must not be null or negative for account: " + accountId);
            }
            this.accountBalances.put(accountId, balance);
        });
    }

    public BatchResult process(List<Transaction> transactions) {
        Objects.requireNonNull(transactions, "Transactions must not be null");

        ArrayList<String> successfulTransactionIds = new ArrayList<>();
        ArrayList<TransactionFailure> failures = new ArrayList<>();
        Set<String> seenTransactionIds = new HashSet<>();

        for (Transaction transaction : transactions) {
            try {
                processTransaction(transaction, seenTransactionIds);
                successfulTransactionIds.add(transaction.transactionId());
            } catch (TransactionProcessingException exception) {
                failures.add(new TransactionFailure(
                        getTransactionId(transaction),
                        exception.getFailureType(),
                        exception.getMessage()));
            }
        }

        return new BatchResult(
                successfulTransactionIds,
                failures,
                accountBalances);
    }

    private void processTransaction(
            Transaction transaction, Set<String> seenTransactionIds) {
        validateTransactionAndTrackId(transaction, seenTransactionIds);

        String accountId = transaction.accountId();
        if (!accountBalances.containsKey(accountId)) {
            throw new AccountNotFoundException("Account not found: " + accountId);
        }

        BigDecimal currentBalance = accountBalances.get(accountId);
        BigDecimal updatedBalance = calculateUpdatedBalance(transaction, currentBalance);

        accountBalances.put(accountId, updatedBalance);
    }

    private static void validateTransactionAndTrackId(
            Transaction transaction, Set<String> seenTransactionIds) {
        if (transaction == null) {
            throw new InvalidTransactionException("Transaction must not be null");
        }
        if (isBlank(transaction.transactionId())) {
            throw new InvalidTransactionException(
                    "Transaction ID must not be null or blank");
        }
        if (!seenTransactionIds.add(transaction.transactionId())) {
            throw new DuplicateTransactionException(
                    "Duplicate transaction ID: " + transaction.transactionId());
        }
        if (isBlank(transaction.accountId())) {
            throw new InvalidTransactionException(
                    "Account ID must not be null or blank");
        }
        if (transaction.type() == null) {
            throw new InvalidTransactionException("Transaction type must not be null");
        }
        if (transaction.amount() == null
                || transaction.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionException(
                    "Transaction amount must be greater than zero");
        }
    }

    private static BigDecimal calculateUpdatedBalance(
            Transaction transaction, BigDecimal currentBalance) {
        if (transaction.type() == TransactionType.CREDIT) {
            return currentBalance.add(transaction.amount());
        }

        if (currentBalance.compareTo(transaction.amount()) < 0) {
            throw new InsufficientBalanceException(
                    "Insufficient balance for account: " + transaction.accountId());
        }
        return currentBalance.subtract(transaction.amount());
    }

    private static String getTransactionId(Transaction transaction) {
        return transaction == null ? null : transaction.transactionId();
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
