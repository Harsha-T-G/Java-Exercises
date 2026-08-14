package org.example.batchtransactionprocessor.exception;

import org.example.batchtransactionprocessor.constants.FailureType;

public class InvalidTransactionException extends TransactionProcessingException {

    public InvalidTransactionException(String message) {
        super(FailureType.INVALID_TRANSACTION, message);
    }
}
