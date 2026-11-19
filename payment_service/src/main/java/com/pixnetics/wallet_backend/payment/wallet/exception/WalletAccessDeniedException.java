package com.pixnetics.wallet_backend.payment.wallet.exception;

public class WalletAccessDeniedException  extends RuntimeException {

    public WalletAccessDeniedException (String message) {
        super(message);
    }
}
