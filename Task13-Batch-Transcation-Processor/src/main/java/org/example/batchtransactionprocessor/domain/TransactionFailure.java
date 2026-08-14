package org.example.batchtransactionprocessor.domain;

import org.example.batchtransactionprocessor.constants.FailureType;

public record TransactionFailure(
        String transactionId,
        FailureType failureType,
        String failureMessage) {
}
