package com.pixnetics.wallet_backend.payment.merchant.dto;

import com.pixnetics.wallet_backend.payment.wallet.dto.WalletResponse;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Merchant account and business profile")
public record MerchantResponse(

        @Schema(description = "Unique identifier of the merchant", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID id,

        @Schema(description = "Merchant login username", example = "acme_store")
        String username,

        @Schema(description = "Registered business name", example = "Acme Store")
        String businessName,

        @Schema(description = "Business category", example = "Retail")
        String category,

        @Schema(description = "Business address", example = "123 Main Street")
        String address,

        @Schema(description = "Business country as an ISO 3166-1 alpha-2 code", example = "DE")
        String countryCode,

        @Schema(description = "Merchant settlement schedule or method", example = "MONTHLY")
        String settleType,

        @Schema(description = "Merchant's main wallet")
        WalletResponse mainWallet
) {}
