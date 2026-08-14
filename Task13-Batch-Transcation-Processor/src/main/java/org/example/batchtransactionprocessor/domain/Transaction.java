package org.example.batchtransactionprocessor.domain;

import org.example.batchtransactionprocessor.constants.TransactionType;

import java.math.BigDecimal;

public record Transaction(
        String transactionId,
        String accountId,
        TransactionType type,
        BigDecimal amount) {
}
