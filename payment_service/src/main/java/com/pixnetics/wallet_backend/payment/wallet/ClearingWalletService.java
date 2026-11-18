package com.pixnetics.wallet_backend.payment.wallet;

import com.pixnetics.wallet_backend.enums.UserRole;
import com.pixnetics.wallet_backend.enums.WalletType;
import com.pixnetics.wallet_backend.payment.user.User;
import com.pixnetics.wallet_backend.payment.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/** This is a temporary class **/
@Service
@RequiredArgsConstructor
@Slf4j
public class ClearingWalletService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void initialize() {

        if (walletRepository.findClearingWallet().isPresent()) {
            log.info("Clearing wallet already exists. Skipping initialization.");
            return;
        }

        User systemUser = userRepository.findSystemUser()
                .orElseGet(this::createSystemUser);

        Wallet clearingWallet = new Wallet();
        clearingWallet.setUser(systemUser);
        clearingWallet.setWalletType(WalletType.CLEARING);
        clearingWallet.setName("Platform Clearing Wallet");
        clearingWallet.setCurrency("EUR");
        clearingWallet.setBalance(new BigDecimal("1000000.00"));
        clearingWallet.setActive(true);

        walletRepository.save(clearingWallet);

        log.info("Platform clearing wallet initialized successfully.");
    }

    private User createSystemUser() {
        User user = new User();
        user.setUsername("system_user");
        user.setPasswordHash(passwordEncoder.encode("password"));
        user.setRole(UserRole.SYSTEM);
        user.setActive(true);

        return userRepository.save(user);
    }
}