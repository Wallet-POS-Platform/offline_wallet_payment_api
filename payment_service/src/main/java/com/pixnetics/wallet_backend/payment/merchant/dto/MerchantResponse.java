package com.pixnetics.wallet_backend.payment.merchant.dto;

import com.pixnetics.wallet_backend.payment.wallet.dto.WalletResponse;

import java.util.UUID;

public record MerchantResponse(
        UUID id,
        String username,
        WalletResponse mainWallet
) {}
