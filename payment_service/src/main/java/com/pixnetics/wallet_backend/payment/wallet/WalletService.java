package com.pixnetics.wallet_backend.payment.wallet;

import com.pixnetics.wallet_backend.enums.TransactionStatus;
import com.pixnetics.wallet_backend.enums.TransactionType;
import com.pixnetics.wallet_backend.enums.UserRole;
import com.pixnetics.wallet_backend.enums.WalletType;
import com.pixnetics.wallet_backend.payment.client.dto.TopUpRequest;
import com.pixnetics.wallet_backend.payment.client.dto.WithdrawRequest;
import com.pixnetics.wallet_backend.payment.client.exception.ClientNotFoundException;
import com.pixnetics.wallet_backend.payment.ledger.Ledger;
import com.pixnetics.wallet_backend.payment.ledger.LedgerRepository;
import com.pixnetics.wallet_backend.payment.transaction.Transaction;
import com.pixnetics.wallet_backend.payment.transaction.TransactionRepository;
import com.pixnetics.wallet_backend.payment.transaction.TransactionResponse;
import com.pixnetics.wallet_backend.payment.transaction.exception.InsufficientFundsException;
import com.pixnetics.wallet_backend.payment.transaction.exception.TransactionException;
import com.pixnetics.wallet_backend.payment.user.User;
import com.pixnetics.wallet_backend.payment.user.UserRepository;
import com.pixnetics.wallet_backend.payment.wallet.dto.WalletCreationRequest;
import com.pixnetics.wallet_backend.payment.wallet.dto.WalletResponse;
import com.pixnetics.wallet_backend.payment.wallet.exception.*;
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
    private final TransactionRepository transactionRepository;
    private final LedgerRepository ledgerRepository;

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
        return executeWalletOperation(userId, request.walletId(), request.amount(), request.currency(), TransactionType.TOPUP);
    }

    @Transactional
    public TransactionResponse withdraw(UUID userId, WithdrawRequest request) {
        return executeWalletOperation(userId, request.walletId(), request.amount(), request.currency(), TransactionType.WITHDRAW);
    }

    /** This is for temporary Logic. We need RabbitMQ message driven Provider service for async provider handling **/
    private TransactionResponse executeWalletOperation(UUID userId, UUID walletId, BigDecimal amount, String currency, TransactionType type) {
        // Always acquire locks in the same order: customer wallet, then clearing wallet.
        Wallet wallet = walletRepository.findLockedWalletById(walletId)
                .orElseThrow(() -> new WalletNotFoundException(userId));

        if(!wallet.getUser().getId().equals(userId)){
            throw new TransactionException("The wallet doesn't belongs to the user");
        }

        if (!wallet.isActive()) {
            throw new WalletInactiveException(wallet.getId());
        }

        if (!wallet.getCurrency().equals(currency)) {
            throw new WalletCurrencyMismatchException(wallet.getCurrency(), currency);
        }

        Wallet clearingWallet = walletRepository.findClearingWallet()
                .orElseThrow(() -> new WalletNotFoundException(WalletType.CLEARING));

        if (!clearingWallet.isActive()) {
            throw new WalletInactiveException(wallet.getId());
        }

        if(wallet.getId().equals(clearingWallet.getId())){
            throw new TransactionException("The clearing wallet cannot be used as a user wallet");
        }

        if (!wallet.getCurrency().equals(clearingWallet.getCurrency())) {
            throw new WalletCurrencyMismatchException(wallet.getCurrency(), clearingWallet.getCurrency());
        }

        Wallet debitWallet;
        Wallet creditWallet;

        if (type == TransactionType.TOPUP) {
            debitWallet = clearingWallet;
            creditWallet = wallet;
        } else {
            debitWallet = wallet;
            creditWallet = clearingWallet;
        }

        if (debitWallet.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException(debitWallet.getId());
        }

        // Update balances.
        debitWallet.setBalance(debitWallet.getBalance().subtract(amount));
        creditWallet.setBalance(creditWallet.getBalance().add(amount));

        // Create transaction.
        Transaction transaction = new Transaction();
        transaction.setType(type);
        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setFromWalletId(debitWallet);
        transaction.setToWalletId(creditWallet);
        transaction.setAmount(amount);
        transaction.setCurrency(wallet.getCurrency());
        transaction.setVatRate(BigDecimal.ZERO);

        transaction = transactionRepository.saveAndFlush(transaction);

        // Create double-entry ledger records.
        Ledger debitEntry = createLedgerEntry(transaction, debitWallet, amount.negate());
        Ledger creditEntry = createLedgerEntry(transaction, creditWallet, amount);

        ledgerRepository.saveAll(List.of(debitEntry, creditEntry));

        log.info("{} completed. Transaction ID: {}, Wallet ID: {}, Amount: {} {}",
                type,
                transaction.getId(),
                userId,
                amount,
                wallet.getCurrency()
        );

        return new TransactionResponse(
                transaction.getId(),
                wallet.getId(),
                transaction.getType(),
                transaction.getStatus(),
                transaction.getAmount(),
                transaction.getCurrency(),
                wallet.getBalance()
        );
    }

    private Ledger createLedgerEntry(Transaction transaction, Wallet wallet, BigDecimal amount) {
        Ledger ledger = new Ledger();
        ledger.setTransaction(transaction);
        ledger.setWallet(wallet);
        ledger.setAmount(amount);
        ledger.setVat(BigDecimal.ZERO);
        ledger.setVatRate(BigDecimal.ZERO);
        ledger.setCurrency(wallet.getCurrency());

        return ledger;
    }
}
