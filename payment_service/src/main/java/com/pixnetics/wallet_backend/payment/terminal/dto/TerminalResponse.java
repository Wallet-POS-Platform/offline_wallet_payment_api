package com.pixnetics.wallet_backend.payment.terminal.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

public record TerminalResponse(

        @Schema(
            description = "Unique identifier of the terminal",
            example = "550e8400-e29b-41d4-a716-446655440000"
        )
        UUID id,

        @Schema(
            description = "Display name of the terminal",
            example = "Main Entrance POS"
        )
        String name
) {}
