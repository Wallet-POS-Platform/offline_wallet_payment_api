package com.pixnetics.wallet_backend.payment.merchant;

import com.pixnetics.wallet_backend.payment.merchant.dto.MerchantCreationRequest;
import com.pixnetics.wallet_backend.payment.merchant.dto.MerchantResponse;
import com.pixnetics.wallet_backend.payment.terminal.TerminalService;
import com.pixnetics.wallet_backend.payment.terminal.dto.TerminalCreationRequest;
import com.pixnetics.wallet_backend.payment.terminal.dto.TerminalResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@Slf4j
@Tag(name = "Merchant Operations", description = "Endpoints for merchant accounts, terminals, and settlement")
@RestController
@RequestMapping("/api/v1/merchants")
@RequiredArgsConstructor
public class MerchantController {

    private final MerchantService merchantService;
    private final TerminalService terminalService;

    @GetMapping("/{id}")
    @Operation(
            summary = "Get a merchant by ID",
            description = "Returns a merchant account, its business profile, and its main wallet using the merchant's unique identifier."
    )
    @ApiResponse(responseCode = "200", description = "Merchant successfully retrieved")
    public ResponseEntity<MerchantResponse> getById(
            @Parameter(description = "Unique identifier of the merchant", required = true)
            @PathVariable UUID id) {
        log.info("Get Merchant by ID: {}", id);
        return ResponseEntity.ok(merchantService.getById(id));
    }

    @PostMapping
    @Operation(
            summary = "Create a merchant account",
            description = "Registers a new merchant account. The username must be unique. A main wallet is automatically created for the merchant."
    )
    @ApiResponse(responseCode = "201", description = "Merchant successfully created")
    public ResponseEntity<MerchantResponse> create(@Valid @RequestBody MerchantCreationRequest request) {
        log.info("Creating a new Merchant");

        MerchantResponse response = merchantService.create(request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/api/v1/merchants/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/terminals/{id}")
    @Operation(
            summary = "Get a terminal by ID",
            description = "Returns a terminal using its unique identifier"
    )
    @ApiResponse(responseCode = "200", description = "Terminal successfully retrieved")
    public ResponseEntity<TerminalResponse> getTerminalById(
            @Parameter(description = "Unique identifier of the terminal", required = true)
            @PathVariable UUID id
    ) {
        log.info("Getting terminal by ID: {}", id);

        return ResponseEntity.ok(terminalService.getTerminalById(id));
    }

    @GetMapping("/{merchantId}/terminals")
    @Operation(
            summary = "Get all terminals of a merchant",
            description = "Returns all active and inactive terminals belonging to the specified merchant"
    )
    @ApiResponse(responseCode = "200", description = "Terminals successfully retrieved")
    public ResponseEntity<List<TerminalResponse>> getAllTerminals(
            @Parameter(description = "Unique identifier of the merchant", required = true)
            @PathVariable UUID merchantId
    ) {
        log.info("Getting all terminals for merchant: {}", merchantId);

        return ResponseEntity.ok(terminalService.getTerminalsByMerchantId(merchantId));
    }

    @PostMapping("/{merchantId}/terminals")
    @Operation(
            summary = "Create a terminal for a merchant",
            description = "Registers a new terminal for an existing merchant."
    )
    @ApiResponse(responseCode = "201", description = "Terminal successfully created")
    public ResponseEntity<TerminalResponse> create(
            @Parameter(description = "Unique identifier of the merchant", required = true)
            @PathVariable UUID merchantId,
            @RequestBody @Valid TerminalCreationRequest request) {
        log.info("Creating a new terminal");

        TerminalResponse response = terminalService.create(merchantId, request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/api/v1/merchants/terminals/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @PostMapping("/settle")
    @Operation(
            summary = "Settle merchant funds",
            description = "Starts merchant settlement."
    )
    @ApiResponse(responseCode = "200", description = "Merchant settlement completed")
    public String settle() {
        return "settle done!";
    }
}
