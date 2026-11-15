package com.pixnetics.wallet_backend.payment.wallet;

import com.pixnetics.wallet_backend.enums.UserRole;
import com.pixnetics.wallet_backend.enums.WalletType;
import com.pixnetics.wallet_backend.payment.client.exception.ClientNotFoundException;
import com.pixnetics.wallet_backend.payment.user.User;
import com.pixnetics.wallet_backend.payment.user.UserRepository;
import com.pixnetics.wallet_backend.payment.wallet.dto.WalletCreationRequest;
import com.pixnetics.wallet_backend.payment.wallet.dto.WalletResponse;
import com.pixnetics.wallet_backend.payment.wallet.exception.WalletCreationException;
import com.pixnetics.wallet_backend.payment.wallet.exception.WalletNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletService {
    private final WalletRepository walletRepository;
    private final UserRepository userRepository;

    @Transactional
    public WalletResponse createWalletForClient(UUID userId, WalletCreationRequest request) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ClientNotFoundException(userId));

        if(!UserRole.CLIENT.equals(user.getRole())){
            throw new WalletCreationException("Wallet Creation not allowed for: " + user.getRole());
        }

        boolean isActiveWalletExits = walletRepository.existsActiveWalletByUserId(userId);

        return this.createWallet(request.name(), user, isActiveWalletExits ? WalletType.SUB : WalletType.MAIN, request.currency());
    }

    public WalletResponse createWallet(String walletName, User user, WalletType type, String currency) {
        Wallet wallet = new Wallet();
        wallet.setUser(user);
        wallet.setWalletType(type);
        wallet.setName(walletName);
        wallet.setCurrency(currency);
        wallet.setBalance(BigDecimal.ZERO);

        Wallet saved = walletRepository.save(wallet);

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<WalletResponse> getClientWalletsById(UUID userId) {
        return walletRepository.findAllWalletsByClientId(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public WalletResponse getMerchantWalletById(UUID userId) {
        Wallet wallet = walletRepository.findMainWalletByMerchantId(userId)
                .orElseThrow(() -> new WalletNotFoundException("No Active Wallet Found for Merchant: " + userId));

        return toResponse(wallet);
    }

    public WalletResponse toResponse(Wallet wallet) {
        return new WalletResponse(
                wallet.getId(),
                wallet.getName(),
                wallet.getWalletType().name(),
                wallet.getBalance(),
                wallet.getCurrency(),
                wallet.isActive()
        );
    }
}
