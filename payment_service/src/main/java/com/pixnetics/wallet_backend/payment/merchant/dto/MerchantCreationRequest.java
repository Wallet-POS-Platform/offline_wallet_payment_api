package com.pixnetics.wallet_backend.payment.merchant.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;


@Schema(description = "Request to register a new merchant")
public record MerchantCreationRequest(

        @Schema(example = "acme_store")
        @NotBlank(message = "Username is required")
        @Size(min = 5, max = 20, message = "Username must be between 5 and 20 characters")
        @Pattern(
                regexp = "^[A-Za-z0-9._-]+$",
                message = "Username can only contain letters, numbers, dots, underscores and hyphens"
        )
        String username,

        @Schema(example = "Secure@12345")
        @NotBlank(message = "Password is required")
        @Size(min = 10, max = 64, message = "Password must be between 10 and 64 characters")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9\\s]).+$",
                message = "Password must contain at least one uppercase letter, one lowercase letter, one digit and one special character"
        )
        String password,

        @Schema(example = "Acme Store")
        @NotBlank(message = "Business name is required")
        String businessName,

        @Schema(example = "Retail")
        String category,

        @Schema(example = "123 Main Street")
        String address,

        @Schema(example = "DE89370400440532013000")
        @Pattern(
                regexp = "^[A-Z]{2}\\d{2}[A-Z0-9]{11,30}$",
                message = "Payout IBAN must be a valid uppercase IBAN without spaces"
        )
        String payoutIBAN,

        @Schema(example = "MONTHLY")
        @NotBlank(message = "Settlement type is required")
        String settleType
) {}