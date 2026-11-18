package com.pixnetics.wallet_backend.payment.wallet.exception;

import java.util.UUID;

public class WalletInactiveException extends RuntimeException {

    public WalletInactiveException(UUID walletId) {
        super("Wallet is inactive: " + walletId);
    }
}