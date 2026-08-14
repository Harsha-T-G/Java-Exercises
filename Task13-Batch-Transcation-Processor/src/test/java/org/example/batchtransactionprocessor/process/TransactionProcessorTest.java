package org.example.batchtransactionprocessor.process;

import org.example.batchtransactionprocessor.constants.FailureType;
import org.example.batchtransactionprocessor.constants.TransactionType;
import org.example.batchtransactionprocessor.domain.BatchResult;
import org.example.batchtransactionprocessor.domain.Transaction;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TransactionProcessorTest {

    @Test
    void givenCreditAndDebitTransactions_whenProcessed_thenUpdatesBalanceInOrder() {
        TransactionProcessor processor = processorWithBalance("ACC-1", "1000.00");
        List<Transaction> transactions = List.of(
                transaction("TX-1", "ACC-1", TransactionType.CREDIT, "200.00"),
                transaction("TX-2", "ACC-1", TransactionType.DEBIT, "500.00"));

        BatchResult result = processor.process(transactions);

        assertEquals(List.of("TX-1", "TX-2"), result.successfulTransactionIds());
        assertEquals(List.of(), result.failures());
        assertAmountEquals("700.00", result.finalAccountBalances().get("ACC-1"));
    }

    @Test
    void givenInvalidTransactionData_whenProcessed_thenRejectsEachInvalidTransaction() {
        TransactionProcessor processor = processorWithBalance("ACC-1", "100.00");
        List<Transaction> transactions = new ArrayList<>();
        transactions.add(null);
        transactions.add(transaction(null, "ACC-1", TransactionType.CREDIT, "10.00"));
        transactions.add(transaction(" ", "ACC-1", TransactionType.CREDIT, "10.00"));
        transactions.add(transaction("TX-1", null, TransactionType.CREDIT, "10.00"));
        transactions.add(transaction("TX-2", " ", TransactionType.CREDIT, "10.00"));
        transactions.add(transaction("TX-3", "ACC-1", null, "10.00"));
        transactions.add(new Transaction("TX-4", "ACC-1", TransactionType.CREDIT, null));
        transactions.add(transaction("TX-5", "ACC-1", TransactionType.CREDIT, "0.00"));
        transactions.add(transaction("TX-6", "ACC-1", TransactionType.DEBIT, "-1.00"));

        BatchResult result = processor.process(transactions);

        assertEquals(List.of(), result.successfulTransactionIds());
        assertEquals(9, result.failures().size());
        assertEquals(List.of(
                        FailureType.INVALID_TRANSACTION,
                        FailureType.INVALID_TRANSACTION,
                        FailureType.INVALID_TRANSACTION,
                        FailureType.INVALID_TRANSACTION,
                        FailureType.INVALID_TRANSACTION,
                        FailureType.INVALID_TRANSACTION,
                        FailureType.INVALID_TRANSACTION,
                        FailureType.INVALID_TRANSACTION,
                        FailureType.INVALID_TRANSACTION),
                result.failures().stream().map(failure -> failure.failureType()).toList());
        assertAmountEquals("100.00", result.finalAccountBalances().get("ACC-1"));
    }

    @Test
    void givenRepeatedTransactionId_whenProcessed_thenRejectsTheDuplicate() {
        TransactionProcessor processor = processorWithBalance("ACC-1", "100.00");
        List<Transaction> transactions = List.of(
                transaction("TX-1", "ACC-1", TransactionType.CREDIT, "25.00"),
                transaction("TX-1", "ACC-1", TransactionType.CREDIT, "50.00"));

        BatchResult result = processor.process(transactions);

        assertEquals(List.of("TX-1"), result.successfulTransactionIds());
        assertEquals(1, result.failures().size());
        assertEquals(FailureType.DUPLICATE_TRANSACTION,
                result.failures().getFirst().failureType());
        assertAmountEquals("125.00", result.finalAccountBalances().get("ACC-1"));
    }

    @Test
    void givenMissingAccount_whenProcessed_thenRejectsTransaction() {
        TransactionProcessor processor = processorWithBalance("ACC-1", "100.00");
        Transaction transaction = transaction(
                "TX-1", "ACC-404", TransactionType.CREDIT, "25.00");

        BatchResult result = processor.process(List.of(transaction));

        assertEquals(List.of(), result.successfulTransactionIds());
        assertEquals(FailureType.ACCOUNT_NOT_FOUND,
                result.failures().getFirst().failureType());
        assertAmountEquals("100.00", result.finalAccountBalances().get("ACC-1"));
    }

    @Test
    void givenDebitGreaterThanBalance_whenProcessed_thenRejectsItWithoutChangingBalance() {
        TransactionProcessor processor = processorWithBalance("ACC-1", "100.00");
        Transaction transaction = transaction(
                "TX-1", "ACC-1", TransactionType.DEBIT, "100.01");

        BatchResult result = processor.process(List.of(transaction));

        assertEquals(List.of(), result.successfulTransactionIds());
        assertEquals(FailureType.INSUFFICIENT_BALANCE,
                result.failures().getFirst().failureType());
        assertAmountEquals("100.00", result.finalAccountBalances().get("ACC-1"));
    }

    @Test
    void givenFailureBetweenValidTransactions_whenProcessed_thenContinuesProcessing() {
        TransactionProcessor processor = processorWithBalance("ACC-1", "1000.00");
        List<Transaction> transactions = List.of(
                transaction("TX-1", "ACC-1", TransactionType.CREDIT, "200.00"),
                transaction("TX-2", "ACC-1", TransactionType.DEBIT, "500.00"),
                transaction("TX-3", "ACC-1", TransactionType.DEBIT, "1000.00"),
                transaction("TX-4", "ACC-1", TransactionType.CREDIT, "100.00"));

        BatchResult result = processor.process(transactions);

        assertEquals(List.of("TX-1", "TX-2", "TX-4"),
                result.successfulTransactionIds());
        assertEquals(List.of("TX-3"),
                result.failures().stream().map(failure -> failure.transactionId()).toList());
        assertEquals(FailureType.INSUFFICIENT_BALANCE,
                result.failures().getFirst().failureType());
        assertAmountEquals("800.00", result.finalAccountBalances().get("ACC-1"));
    }

    @Test
    void givenInterleavedSuccessesAndFailures_whenProcessed_thenPreservesResultOrder() {
        TransactionProcessor processor = processorWithBalance("ACC-1", "50.00");
        List<Transaction> transactions = List.of(
                transaction("TX-1", "ACC-1", TransactionType.CREDIT, "10.00"),
                transaction("TX-2", "MISSING", TransactionType.CREDIT, "10.00"),
                transaction("TX-3", "ACC-1", TransactionType.DEBIT, "20.00"),
                transaction("TX-4", "ACC-1", TransactionType.DEBIT, "100.00"),
                transaction("TX-5", "ACC-1", TransactionType.CREDIT, "5.00"));

        BatchResult result = processor.process(transactions);

        assertEquals(List.of("TX-1", "TX-3", "TX-5"),
                result.successfulTransactionIds());
        assertEquals(List.of("TX-2", "TX-4"),
                result.failures().stream().map(failure -> failure.transactionId()).toList());
        assertAmountEquals("45.00", result.finalAccountBalances().get("ACC-1"));
    }

    @Test
    void givenOrderedInitialBalances_whenProcessed_thenPreservesAccountOrder() {
        Map<String, BigDecimal> balances = new LinkedHashMap<>();
        balances.put("ACC-2", new BigDecimal("200.00"));
        balances.put("ACC-1", new BigDecimal("100.00"));
        TransactionProcessor processor = new TransactionProcessor(balances);

        BatchResult result = processor.process(List.of());

        assertEquals(List.of("ACC-2", "ACC-1"),
                new ArrayList<>(result.finalAccountBalances().keySet()));
    }

    @Test
    void givenCallerBalanceMap_whenTransactionsAreProcessed_thenDoesNotModifyCallerMap() {
        Map<String, BigDecimal> balances = new LinkedHashMap<>();
        balances.put("ACC-1", new BigDecimal("100.00"));
        TransactionProcessor processor = new TransactionProcessor(balances);

        processor.process(List.of(
                transaction("TX-1", "ACC-1", TransactionType.CREDIT, "25.00")));

        assertAmountEquals("100.00", balances.get("ACC-1"));
    }

    @Test
    void givenBatchResultCollections_whenModificationIsAttempted_thenTheyAreImmutable() {
        TransactionProcessor processor = processorWithBalance("ACC-1", "100.00");
        BatchResult result = processor.process(List.of(
                transaction("TX-1", "ACC-1", TransactionType.CREDIT, "10.00"),
                transaction("TX-2", "MISSING", TransactionType.DEBIT, "5.00")));

        assertThrows(UnsupportedOperationException.class,
                () -> result.successfulTransactionIds().add("TX-3"));
        assertThrows(UnsupportedOperationException.class, result.failures()::clear);
        assertThrows(UnsupportedOperationException.class,
                () -> result.finalAccountBalances().put("ACC-2", BigDecimal.TEN));
    }

    private static TransactionProcessor processorWithBalance(
            String accountId, String balance) {
        return new TransactionProcessor(
                Map.of(accountId, new BigDecimal(balance)));
    }

    private static Transaction transaction(
            String transactionId,
            String accountId,
            TransactionType type,
            String amount) {
        return new Transaction(
                transactionId,
                accountId,
                type,
                new BigDecimal(amount));
    }

    private static void assertAmountEquals(String expected, BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual));
    }
}
