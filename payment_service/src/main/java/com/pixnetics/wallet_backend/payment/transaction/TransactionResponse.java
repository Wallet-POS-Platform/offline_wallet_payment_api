package com.pixnetics.wallet_backend.payment.transaction;

import com.pixnetics.wallet_backend.enums.TransactionStatus;
import com.pixnetics.wallet_backend.enums.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Result of a wallet top-up or withdrawal")
public record TransactionResponse(

        @Schema(
                description = "Unique identifier of the transaction",
                example = "550e8400-e29b-41d4-a716-446655440000"
        )
        UUID transactionId,

        @Schema(
                description = "Unique identifier of the affected wallet",
                example = "650e8400-e29b-41d4-a716-446655440000"
        )
        UUID walletId,

        @Schema(
                description = "Transaction type",
                example = "TOPUP"
        )
        TransactionType type,

        @Schema(
                description = "Transaction status",
                example = "SUCCESS"
        )
        TransactionStatus status,

        @Schema(
                description = "Transaction amount",
                example = "50.00"
        )
        BigDecimal amount,

        @Schema(
                description = "Wallet currency",
                example = "EUR"
        )
        String currency,

        @Schema(
                description = "Wallet balance after the transaction",
                example = "150.00"
        )
        BigDecimal resultingBalance
) {}