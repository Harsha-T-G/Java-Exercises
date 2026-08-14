package org.example.batchtransactionprocessor.exception;

import org.example.batchtransactionprocessor.constants.FailureType;

public class AccountNotFoundException extends TransactionProcessingException {

    public AccountNotFoundException(String message) {
        super(FailureType.ACCOUNT_NOT_FOUND, message);
    }
}
