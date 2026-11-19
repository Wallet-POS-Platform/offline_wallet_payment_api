package com.pixnetics.wallet_backend.payment.transaction;

import com.pixnetics.wallet_backend.enums.TransactionStatus;
import com.pixnetics.wallet_backend.enums.TransactionType;
import com.pixnetics.wallet_backend.payment.ledger.Ledger;
import com.pixnetics.wallet_backend.payment.ledger.LedgerRepository;
import com.pixnetics.wallet_backend.payment.transaction.exception.InsufficientFundsException;
import com.pixnetics.wallet_backend.payment.wallet.Wallet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final LedgerRepository ledgerRepository;

    public TransactionResponse performTopup(Wallet debitWallet, Wallet creditWallet, BigDecimal amount){
        return doTransaction(debitWallet, creditWallet, amount, BigDecimal.ZERO, TransactionType.TOPUP, TransactionStatus.SUCCESS);
    }

    public TransactionResponse performWithdraw(Wallet debitWallet, Wallet creditWallet, BigDecimal amount){
        return doTransaction(debitWallet, creditWallet, amount, BigDecimal.ZERO, TransactionType.WITHDRAW, TransactionStatus.SUCCESS);
    }

    private TransactionResponse doTransaction(Wallet debitWallet,
                                              Wallet creditWallet,
                                              BigDecimal amount,
                                              BigDecimal vatRate,
                                              TransactionType type,
                                              TransactionStatus status) {

        if (debitWallet.getBalance().compareTo(amount) < 0) {
            log.error("Insufficient funds in wallet: {}, balance: {}, requiredAmount: {}", debitWallet.getId(), debitWallet.getBalance(), amount);
            throw new InsufficientFundsException(debitWallet.getId());
        }

        debitWallet.setBalance(debitWallet.getBalance().subtract(amount));
        creditWallet.setBalance(creditWallet.getBalance().add(amount));

        // Create transaction.
        Transaction transaction = new Transaction();
        transaction.setType(type);
        transaction.setStatus(status);
        transaction.setFromWalletId(debitWallet);
        transaction.setToWalletId(creditWallet);
        transaction.setAmount(amount);
        transaction.setCurrency(debitWallet.getCurrency());
        transaction.setVatRate(vatRate);

        transaction = transactionRepository.saveAndFlush(transaction);

        // Create double-entry ledger records.
        Ledger debitEntry = createLedgerEntry(transaction, debitWallet, amount.negate(), vatRate);
        Ledger creditEntry = createLedgerEntry(transaction, creditWallet, amount, vatRate);

        ledgerRepository.saveAll(List.of(debitEntry, creditEntry));

        log.info("Transaction Id:{} Type: {} Status:{} Amount: {} {}",
                transaction.getId(),
                type,
                status,
                amount,
                debitWallet.getCurrency()
        );

        return new TransactionResponse(
                transaction.getId(),
                transaction.getType(),
                transaction.getStatus(),
                transaction.getAmount(),
                transaction.getCurrency()
        );
    }

    private Ledger createLedgerEntry(Transaction transaction, Wallet wallet, BigDecimal amount, BigDecimal vatRate) {

        Ledger ledger = new Ledger();
        ledger.setTransaction(transaction);
        ledger.setWallet(wallet);
        ledger.setAmount(amount);
        ledger.setVat(BigDecimal.ZERO);
        ledger.setVatRate(vatRate);
        ledger.setCurrency(wallet.getCurrency());

        return ledger;
    }
}
