package com.pixnetics.wallet_backend.payment.wallet.exception;

public class WalletException extends RuntimeException {

    public WalletException(String username) {
        super("Wallet is already exits for: " + username);
    }
}
