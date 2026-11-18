package com.pixnetics.wallet_backend.payment.client.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Request to top up from a wallet")
public record TopUpRequest(


        @Schema(description = "Transaction amount. Must be greater than zero and have at most two decimal places")
        @NotNull(message = "Amount is required")
        @DecimalMin(value = "100.00", message = "Amount must be greater than zero")
        @Digits(
                integer = 8,
                fraction = 2,
                message = "Amount must have at most 8 integer digits and 2 decimal places"
        )
        BigDecimal amount
) {}