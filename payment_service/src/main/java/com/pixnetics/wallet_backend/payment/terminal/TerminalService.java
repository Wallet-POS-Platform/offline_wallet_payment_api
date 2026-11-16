package com.pixnetics.wallet_backend.payment.terminal;

import com.pixnetics.wallet_backend.payment.merchant.Merchant;
import com.pixnetics.wallet_backend.payment.merchant.MerchantRepository;
import com.pixnetics.wallet_backend.payment.merchant.exception.MerchantNotFoundException;
import com.pixnetics.wallet_backend.payment.terminal.dto.TerminalCreationRequest;
import com.pixnetics.wallet_backend.payment.terminal.dto.TerminalResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TerminalService {
    private final TerminalRepository terminalRepository;
    private final MerchantRepository merchantRepository;

    @Transactional(readOnly = true)
    public List<TerminalResponse> getTerminalsByMerchantId(UUID merchantId) {
        log.info("Getting terminals for merchant: {}", merchantId);

        if (!merchantRepository.existsById(merchantId)) {
            throw new MerchantNotFoundException(merchantId);
        }

        return terminalRepository.findAllByMerchantId(merchantId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public TerminalResponse create(UUID merchantId, TerminalCreationRequest request) {
        log.info("Creating terminal for merchant: {}", merchantId);

        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new MerchantNotFoundException(merchantId));

        Terminal terminal = new Terminal();
        terminal.setMerchant(merchant);
        terminal.setName(request.name());
        terminal.setCertId(request.certificateSerialNumber());

        return toResponse(terminalRepository.save(terminal));
    }

    private TerminalResponse toResponse(Terminal terminal) {
        return new TerminalResponse(
            terminal.getId(),
            terminal.getName()
        );
    }
}
