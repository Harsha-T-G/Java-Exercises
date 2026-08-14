package org.example.batchtransactionprocessor.exception;

import org.example.batchtransactionprocessor.constants.FailureType;

public abstract class TransactionProcessingException extends RuntimeException {

    private final FailureType failureType;

    protected TransactionProcessingException(FailureType failureType, String message) {
        super(message);
        this.failureType = failureType;
    }

    public FailureType getFailureType() {
        return failureType;
    }
}
