package com.pixnetics.wallet_backend.payment.terminal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to register a new terminal")
public record TerminalCreationRequest(

        @Schema(
                description = "Display name of the terminal",
                example = "Main Entrance POS"
        )
        @NotBlank(message = "Terminal name is required")
        @Size(max = 100, message = "Terminal name must not exceed 100 characters")
        String name,

        @Schema(
                description = "Serial number of the terminal's client certificate",
                example = "4A:8F:2B:91:C3:D7:E5:10"
        )
        @NotBlank(message = "Certificate serial number is required")
        @Size(max = 255, message = "Certificate serial number must not exceed 255 characters")
        String certificateSerialNumber
) {}
