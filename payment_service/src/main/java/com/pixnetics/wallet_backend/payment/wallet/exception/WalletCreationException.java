package com.pixnetics.wallet_backend.payment.wallet.exception;

import com.pixnetics.wallet_backend.payment.user.User;

public class WalletCreationException extends RuntimeException {

    public WalletCreationException(String message) {
        super(message);
    }
}
