package com.pixnetics.wallet_backend.payment.client;

import com.pixnetics.wallet_backend.payment.client.dto.ClientCreationRequest;
import com.pixnetics.wallet_backend.payment.client.dto.ClientResponse;
import com.pixnetics.wallet_backend.payment.wallet.WalletService;
import com.pixnetics.wallet_backend.payment.wallet.dto.WalletCreationRequest;
import com.pixnetics.wallet_backend.payment.wallet.dto.WalletResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@Slf4j
@Tag(name = "Client Operations")
@RestController
@RequestMapping("/api/v1/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;
    private final WalletService walletService;

    @GetMapping("/{id}")
    @Operation(
            summary = "Get a client by ID",
            description = "Returns a client account and its associated wallets using the client's unique identifier."
    )
    @ApiResponse(responseCode = "200", description = "Client successfully retrieved")
    public ResponseEntity<ClientResponse> getById(
            @Parameter(description = "Unique identifier of the client", required = true)
            @PathVariable UUID id) {
        log.info("Get Client by ID: {}", id);
        return ResponseEntity.ok(clientService.getById(id));
    }

    @GetMapping()
    @Operation(
            summary = "Get a client by username",
            description = "Returns a client account and its associated wallets using the client's unique username."
    )
    @ApiResponse(responseCode = "200", description = "Client successfully retrieved")
    public ResponseEntity<ClientResponse> getByUsername(
            @Parameter(description = "Unique username of the client", required = true, example = "user123")
            @RequestParam String username) {
        log.info("Get Client by username: {}", username);
        return ResponseEntity.ok(clientService.getByUsername(username));
    }

    @PostMapping
    @Operation(
            summary = "Create a client account",
            description = "Registers a new client account on the wallet platform. The username must be unique. A main wallet is automatically created for the client."
    )
    @ApiResponse(responseCode = "201", description = "Client successfully created")
    public ResponseEntity<ClientResponse> createClient(@Valid @RequestBody ClientCreationRequest request) {
        log.info("Request received for Creating a new client");

        ClientResponse response = clientService.create(request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/api/v1/clients/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @PostMapping("/{id}/wallet")
    @Operation(
            summary = "Create a wallet for a client",
            description = "Creates an additional wallet for an existing client."
    )
    @ApiResponse(responseCode = "201", description = "Wallet successfully created")
    public ResponseEntity<WalletResponse> createWallet(
                                                    @Parameter(description = "Unique identifier of the client", required = true)
                                                    @PathVariable UUID id,
                                                    @Valid @RequestBody WalletCreationRequest request) {
        log.info("Creating a new wallet");
        WalletResponse response = walletService.create(id, request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @PostMapping("/topup")
    public String topup() {
        log.info("Doing topup");
        return "topup done!";
    }

    @PostMapping("/withdraw")
    public String withdraw() {
        return "withdraw done!";
    }


    @GetMapping("/details")
    public String getDetails() {
        log.info("Creating a new client");
        return "Client created!";
    }
}
