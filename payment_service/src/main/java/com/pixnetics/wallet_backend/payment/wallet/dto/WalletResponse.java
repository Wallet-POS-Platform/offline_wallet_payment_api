package com.pixnetics.wallet_backend.payment.wallet.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.UUID;

public record WalletResponse(

        @Schema(example = "550e8400-e29b-41d4-a716-446655440001")
        UUID id,

        @Schema(example = "Main Wallet")
        String name,

        @Schema(example = "MAIN")
        String type,

        @Schema(description = "Available wallet balance", example = "1250.50")
        BigDecimal balance,

        @Schema(example = "EUR")
        String currency,

        @Schema(example = "true")
        boolean isActive
) {}