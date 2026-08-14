package org.example;

import org.example.batchtransactionprocessor.constants.TransactionType;
import org.example.batchtransactionprocessor.domain.BatchResult;
import org.example.batchtransactionprocessor.domain.Transaction;
import org.example.batchtransactionprocessor.process.TransactionProcessor;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Main {

    public static void main(String[] args) {
        Map<String, BigDecimal> initialBalances = new LinkedHashMap<>();
        initialBalances.put("ACC-1", new BigDecimal("1000.00"));

        List<Transaction> transactions = List.of(
                transaction("TX-1", TransactionType.CREDIT, "200.00"),
                transaction("TX-2", TransactionType.DEBIT, "500.00"),
                transaction("TX-3", TransactionType.DEBIT, "1000.00"),
                transaction("TX-4", TransactionType.CREDIT, "100.00"));

        TransactionProcessor processor = new TransactionProcessor(initialBalances);
        BatchResult result = processor.process(transactions);

        System.out.println("Successful transaction IDs: "
                + result.successfulTransactionIds());
        System.out.println("Failures:");
        result.failures().forEach(failure -> System.out.println(
                failure.transactionId() + " - " + failure.failureType()
                        + ": " + failure.failureMessage()));
        System.out.println("Final account balances: " + result.finalAccountBalances());
    }

    private static Transaction transaction(
            String transactionId, TransactionType type, String amount) {
        return new Transaction(
                transactionId,
                "ACC-1",
                type,
                new BigDecimal(amount));
    }
}
