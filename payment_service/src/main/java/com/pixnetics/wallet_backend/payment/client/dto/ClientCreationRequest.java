package com.pixnetics.wallet_backend.payment.client.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to register a new client")
public record ClientCreationRequest(

        @Schema(example = "user123")
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

        @Schema(example = "Jane")
        @NotBlank(message = "First name is required")
        String firstName,

        @Schema(example = "Doe")
        @NotBlank(message = "Last name is required")
        String lastName,

        @Schema(example = "123 Main Street")
        String address,

        @Schema(example = "+491234567890")
        String phoneNumber,

        @Schema(example = "DE")
        @NotBlank(message = "Country is required")
        @Pattern(
                regexp = "^[A-Z]{2}$",
                message = "Country must be a two-letter ISO 3166-1 alpha-2 code"
        )
        String countryCode,

        @Schema(example = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCV9YPeqeKETHDebglg+Y4fj2RI")
        @NotBlank(message = "Public key is required")
        @Size(max = 4096, message = "Public key is too long")
        String publicKey
) {}
