package org.example.batchtransactionprocessor.exception;

import org.example.batchtransactionprocessor.constants.FailureType;

public class DuplicateTransactionException extends TransactionProcessingException {

    public DuplicateTransactionException(String message) {
        super(FailureType.DUPLICATE_TRANSACTION, message);
    }
}
