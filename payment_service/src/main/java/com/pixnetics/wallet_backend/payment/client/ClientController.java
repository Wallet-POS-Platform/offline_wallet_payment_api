package com.pixnetics.wallet_backend.payment.client;

import com.pixnetics.wallet_backend.payment.client.dto.ClientCreationRequest;
import com.pixnetics.wallet_backend.payment.client.dto.ClientResponse;
import com.pixnetics.wallet_backend.payment.client.dto.TopUpRequest;
import com.pixnetics.wallet_backend.payment.client.dto.WithdrawRequest;
import com.pixnetics.wallet_backend.payment.transaction.TransactionResponse;
import com.pixnetics.wallet_backend.payment.wallet.WalletService;
import com.pixnetics.wallet_backend.payment.wallet.dto.WalletCreationRequest;
import com.pixnetics.wallet_backend.payment.wallet.dto.WalletResponse;
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
            @Parameter(description = "Unique username of the client", required = true, example = "jane123")
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
            description = "Creates an sub wallet for an existing client."
    )
    @ApiResponse(responseCode = "201", description = "Wallet successfully created")
    public ResponseEntity<WalletResponse> createWallet(
                                                    @Parameter(description = "Unique identifier of the client", required = true)
                                                    @PathVariable UUID id,
                                                    @Valid @RequestBody WalletCreationRequest request) {
        log.info("Creating a new wallet");
        WalletResponse response = walletService.createWalletForClient(id, request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/{id}/wallets")
    @Operation(
            summary = "Get all the wallets of a given client",
            description = "Give all active and inactive wallets"
    )
    @ApiResponse(responseCode = "200", description = "Wallets successfully retrieved")
    public ResponseEntity<List<WalletResponse>> getAllWallet(
            @Parameter(description = "Unique identifier of the client", required = true)
            @PathVariable UUID id) {

        log.info("Getting all wallets");
        return ResponseEntity.ok(walletService.getClientWalletsById(id));
    }

    @PostMapping("/{id}/topup")
    @Operation(
            summary = "Top up a wallet",
            description = "Adds funds to the specified wallet from the internal clearing wallet"
    )
    @ApiResponse(responseCode = "201", description = "Wallet successfully topped up")
    public ResponseEntity<TransactionResponse> topup(
            @Parameter(description = "Unique identifier of the user", required = true)
            @PathVariable UUID id,

            @Valid @RequestBody TopUpRequest request
    ) {
        log.info("Received top-up request for user: {}", id);

        TransactionResponse response = walletService.topup(id, request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.transactionId())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @PostMapping("/{id}/withdraw")
    @Operation(
            summary = "Withdraw from a wallet",
            description = "Withdraws funds from the specified wallet to the internal clearing wallet"
    )
    @ApiResponse(responseCode = "201", description = "Withdrawal successfully completed")
    public ResponseEntity<TransactionResponse> withdraw(
            @Parameter(description = "Unique identifier of the user", required = true)
            @PathVariable UUID id,

            @Valid @RequestBody WithdrawRequest request
    ) {
        log.info("Received withdrawal request for user: {}", id);

        TransactionResponse response = walletService.withdraw(id, request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.transactionId())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }


    @GetMapping("/details")
    public String getDetails() {
        log.info("Creating a new client");
        return "Client created!";
    }
}
