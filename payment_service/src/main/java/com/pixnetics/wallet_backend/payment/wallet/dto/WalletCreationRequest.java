package com.pixnetics.wallet_backend.payment.wallet.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to create a wallet for a client")
public record WalletCreationRequest(
        @Schema(description = "Display name for the wallet", example = "Travel Wallet")
        @NotBlank(message = "Wallet name is required")
        @Size(min = 5, max = 20, message = "Wallet name must be between 5 and 20 characters")
        String name,

        @Schema(description = "Three-letter ISO 4217 currency code", example = "EUR")
        @NotBlank(message = "Currency is required")
        @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a three-letter uppercase ISO 4217 code")
        String currency
) {}
