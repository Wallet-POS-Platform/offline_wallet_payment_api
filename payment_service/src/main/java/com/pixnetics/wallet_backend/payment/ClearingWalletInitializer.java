package com.pixnetics.wallet_backend.payment;

import com.pixnetics.wallet_backend.payment.wallet.ClearingWalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClearingWalletInitializer implements ApplicationRunner {

    private final ClearingWalletService clearingWalletService;

    @Override
    public void run(@NonNull ApplicationArguments args) {
        clearingWalletService.initialize();
    }
}
