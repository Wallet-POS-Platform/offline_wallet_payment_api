package com.pixnetics.wallet_backend.common;

import com.pixnetics.wallet_backend.payment.wallet.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseInitializer implements ApplicationRunner {

    private final WalletService walletService;

    @Override
    public void run(@NonNull ApplicationArguments args) {
        walletService.initializeClearingWallet();
    }
}
