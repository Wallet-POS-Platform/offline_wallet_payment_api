package com.pixnetics.wallet_backend.payment.wallet;

import com.pixnetics.wallet_backend.common.enums.UserRole;
import com.pixnetics.wallet_backend.common.enums.WalletType;
import com.pixnetics.wallet_backend.payment.client.dto.TopUpRequest;
import com.pixnetics.wallet_backend.payment.client.dto.WithdrawRequest;
import com.pixnetics.wallet_backend.payment.transaction.TransactionResponse;
import com.pixnetics.wallet_backend.payment.transaction.TransactionService;
import com.pixnetics.wallet_backend.payment.transaction.exception.CurrencyMismatchException;
import com.pixnetics.wallet_backend.payment.transaction.exception.TransactionException;
import com.pixnetics.wallet_backend.payment.user.User;
import com.pixnetics.wallet_backend.payment.user.UserService;
import com.pixnetics.wallet_backend.payment.wallet.dto.WalletCreationRequest;
import com.pixnetics.wallet_backend.payment.wallet.dto.WalletResponse;
import com.pixnetics.wallet_backend.payment.wallet.exception.WalletAccessDeniedException;
import com.pixnetics.wallet_backend.payment.wallet.exception.WalletCreationException;
import com.pixnetics.wallet_backend.payment.wallet.exception.WalletInactiveException;
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
    private final UserService userService;
    private final TransactionService transactionService;

    private final WalletRepository walletRepository;

    @Transactional
    public void initializeClearingWallet() {

        if (walletRepository.findClearingWallet().isPresent()) {
            log.info("Clearing wallet already exists. Skipping initialization.");
            return;
        }

        User systemUser = userService.getSystemUser();

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

    @Transactional
    public WalletResponse createWalletForClient(UUID userId, WalletCreationRequest request) {
        User user = userService.findById(userId);

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
                .orElseThrow(() -> new WalletNotFoundException(userId));

        return toResponse(wallet);
    }

    private WalletResponse toResponse(Wallet wallet) {
        return new WalletResponse(
                wallet.getId(),
                wallet.getName(),
                wallet.getWalletType().name(),
                wallet.getBalance(),
                wallet.getCurrency(),
                wallet.isActive()
        );
    }

    @Transactional
    public TransactionResponse topup(UUID userId, TopUpRequest request) {
        Wallet userWallet = walletRepository.findLockedWalletById(request.walletId())
                .orElseThrow(() -> new WalletNotFoundException(userId));

        log.info("Topup wallet is found");

        if(!userWallet.getUser().getId().equals(userId)){
            throw new WalletAccessDeniedException ("Not authorized to access provided wallet");
        }
        log.info("Topup wallet is belongs to the requested user");

        if (!userWallet.isActive()) {
            throw new WalletInactiveException(userWallet.getId());
        }
        log.info("Topup Wallet is active");

        Wallet clearingWallet = walletRepository.findClearingWallet()
                .orElseThrow(() -> new WalletNotFoundException(WalletType.CLEARING));

        if (!clearingWallet.isActive()) {
            throw new WalletInactiveException(userWallet.getId());
        }
        log.info("Clearing wallet is found");

        if(userWallet.getId().equals(clearingWallet.getId())){
            throw new TransactionException("The clearing wallet cannot be used as a user wallet");
        }

        if (!userWallet.getCurrency().equals(clearingWallet.getCurrency())) {
            throw new CurrencyMismatchException(userWallet.getCurrency(), clearingWallet.getCurrency());
        }

        transactionService.setActor(userId);

        log.info("Performing the topup request");
        return transactionService.performTopup(clearingWallet, userWallet, request.amount());
    }

    @Transactional
    public TransactionResponse withdraw(UUID userId, WithdrawRequest request) {
        Wallet userWallet = walletRepository.findLockedWalletById(request.walletId())
                .orElseThrow(() -> new WalletNotFoundException(userId));

        log.info("Withdraw wallet is found");

        if(!userWallet.getUser().getId().equals(userId)){
            throw new WalletAccessDeniedException ("Not authorized to access provided wallet");
        }
        log.info("Withdraw wallet is belongs to the authorized user");

        if (!userWallet.isActive()) {
            throw new WalletInactiveException(userWallet.getId());
        }
        log.info("Withdraw Wallet is active");

        Wallet clearingWallet = walletRepository.findClearingWallet()
                .orElseThrow(() -> new WalletNotFoundException(WalletType.CLEARING));

        if (!clearingWallet.isActive()) {
            throw new WalletInactiveException(userWallet.getId());
        }
        log.info("Clearing wallet is found");

        if(userWallet.getId().equals(clearingWallet.getId())){
            throw new TransactionException("The clearing wallet cannot be used as a user wallet");
        }

        if (!userWallet.getCurrency().equals(clearingWallet.getCurrency())) {
            throw new CurrencyMismatchException(userWallet.getCurrency(), clearingWallet.getCurrency());
        }

        transactionService.setActor(userId);

        log.info("Performing the withdraw request");
        return transactionService.performWithdraw(userWallet, clearingWallet, request.amount());
    }
}
