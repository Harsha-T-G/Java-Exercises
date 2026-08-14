package org.example.batchtransactionprocessor.exception;

import org.example.batchtransactionprocessor.constants.FailureType;

public class InsufficientBalanceException extends TransactionProcessingException {

    public InsufficientBalanceException(String message) {
        super(FailureType.INSUFFICIENT_BALANCE, message);
    }
}
