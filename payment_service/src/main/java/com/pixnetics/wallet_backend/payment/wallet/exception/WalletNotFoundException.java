package com.pixnetics.wallet_backend.payment.wallet.exception;

import com.pixnetics.wallet_backend.enums.WalletType;

import java.util.UUID;

public class WalletNotFoundException extends RuntimeException {

    public WalletNotFoundException(UUID userId) {
        super("No Active Wallet Found for User: " + userId);
    }

    public WalletNotFoundException(WalletType type) {
        super("No Active " + type.name() + " Wallet Found");
    }
}
