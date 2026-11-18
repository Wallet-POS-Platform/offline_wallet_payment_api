package com.pixnetics.wallet_backend.payment.transaction.exception;

public class TransactionException extends RuntimeException {

    public TransactionException(String message) {
        super(message);
    }
}