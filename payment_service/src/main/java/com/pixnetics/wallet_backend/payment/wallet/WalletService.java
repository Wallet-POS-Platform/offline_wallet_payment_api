package com.pixnetics.wallet_backend.payment.wallet;

import com.pixnetics.wallet_backend.enums.UserRole;
import com.pixnetics.wallet_backend.enums.WalletType;
import com.pixnetics.wallet_backend.payment.client.exception.ClientNotFoundException;
import com.pixnetics.wallet_backend.payment.user.User;
import com.pixnetics.wallet_backend.payment.user.UserRepository;
import com.pixnetics.wallet_backend.payment.wallet.dto.WalletCreationRequest;
import com.pixnetics.wallet_backend.payment.wallet.dto.WalletResponse;
import com.pixnetics.wallet_backend.payment.wallet.exception.WalletException;
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
    public WalletResponse create(UUID userId, WalletCreationRequest request) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ClientNotFoundException(userId));

        if (UserRole.CLIENT.equals(user.getRole())) {
            return this.createWallet(user, request, WalletType.SUB);
        }

        throw new WalletException(user.getId().toString());
    }

    public WalletResponse createWallet(User user, WalletCreationRequest request, WalletType type) {
        Wallet wallet = new Wallet();
        wallet.setUser(user);
        wallet.setWalletType(type);
        wallet.setName(request.name());
        wallet.setCurrency(request.currency());
        wallet.setBalance(BigDecimal.ZERO);

        Wallet saved = walletRepository.save(wallet);

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<WalletResponse> getWalletsByUserId(UUID userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ClientNotFoundException(userId));

        if (UserRole.CLIENT.equals(user.getRole()) || UserRole.MERCHANT.equals(user.getRole())) {
            return walletRepository.findAllByUser_Id(userId)
                    .stream()
                    .map(this::toResponse)
                    .toList();
        }

        throw new WalletException(user.getId().toString());
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
