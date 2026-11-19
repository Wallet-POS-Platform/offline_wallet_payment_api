package com.pixnetics.wallet_backend.payment.client.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Request to top up a wallet")
public record TopUpRequest(

        @Schema(description = "ID of the wallet to top up")
        @NotNull(message = "Wallet ID is required")
        UUID walletId,

        @Schema(description = "Top-up amount in the wallet currency")
        @NotNull(message = "Amount is required")
        @DecimalMin(value = "10.00", message = "Amount must be at least {value}")
        @Digits(integer = 8, fraction = 2, message = "Amount must have at most {integer} integer digits and {fraction} decimal places")
        BigDecimal amount
) {}