package com.pixnetics.wallet_backend.payment.client.dto;

import com.pixnetics.wallet_backend.payment.wallet.dto.WalletResponse;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.UUID;


public record ClientResponse(

        @Schema(description = "Client unique ID in the system")
        UUID id,

        @Schema(description = "Client username", example = "jane123")
        String username,

        List<WalletResponse> wallets
) {}
