package com.pixnetics.wallet_backend.payment.transaction.exception;

public class CurrencyMismatchException extends TransactionException {

    public CurrencyMismatchException(String fromCurrency, String toCurrency) {
        super("Wallet currency " + fromCurrency + " does not match clearing wallet currency " + toCurrency);
    }
}