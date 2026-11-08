package com.pixnetics.wallet_backend.payment.merchant;

import com.pixnetics.wallet_backend.payment.merchant.dto.MerchantCreationRequest;
import com.pixnetics.wallet_backend.payment.merchant.dto.MerchantResponse;
import com.pixnetics.wallet_backend.payment.terminal.TerminalService;
import com.pixnetics.wallet_backend.payment.terminal.dto.TerminalCreationRequest;
import com.pixnetics.wallet_backend.payment.terminal.dto.TerminalResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@Slf4j
@Tag(name = "Merchant Operations")
@RestController
@RequestMapping("/api/v1/merchants")
@RequiredArgsConstructor
public class MerchantController {

    private final MerchantService merchantService;
    private final TerminalService terminalService;

    @GetMapping("/{id}")
    public ResponseEntity<MerchantResponse> getById(@PathVariable UUID id) {
        log.info("Get Merchant by ID: {}", id);
        return ResponseEntity.ok(merchantService.getById(id));
    }

    @PostMapping
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

    @PostMapping("/{merchantId}/terminals")
    public ResponseEntity<TerminalResponse> create(@PathVariable UUID merchantId,
                                                   @RequestBody @Valid TerminalCreationRequest request) {
        log.info("Creating a new terminal");

        TerminalResponse response = terminalService.create(merchantId, request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/api/v1/terminals/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @PostMapping("/settle")
    public String settle() {
        return "settle done!";
    }
}
