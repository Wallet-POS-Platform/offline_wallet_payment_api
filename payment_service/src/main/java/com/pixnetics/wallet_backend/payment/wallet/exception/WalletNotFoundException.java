package com.pixnetics.wallet_backend.payment.wallet.exception;

import com.pixnetics.wallet_backend.common.enums.WalletType;

import java.util.UUID;

public class WalletNotFoundException extends RuntimeException {

    public WalletNotFoundException(UUID userId) {
        super("Wallet cannot found for the user: " + userId);
    }

    public WalletNotFoundException(WalletType type) {
        super("No Active " + type.name() + " Wallet Found");
    }
}
