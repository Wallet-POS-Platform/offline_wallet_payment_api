package com.pixnetics.wallet_backend.payment.wallet.exception;

public class WalletCurrencyMismatchException extends RuntimeException {

    public WalletCurrencyMismatchException(String walletCurrency, String clearingCurrency) {
        super("Wallet currency " + walletCurrency + " does not match clearing wallet currency " + clearingCurrency);
    }
}