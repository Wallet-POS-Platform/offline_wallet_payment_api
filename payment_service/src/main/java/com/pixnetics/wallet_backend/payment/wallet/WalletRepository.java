package com.pixnetics.wallet_backend.payment.wallet;

import com.pixnetics.wallet_backend.enums.WalletType;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WalletRepository extends JpaRepository<Wallet, UUID> {

    @Query("SELECT w FROM Wallet w WHERE w.user.id = :userId AND w.user.role = UserRole.CLIENT")
    List<Wallet> findAllWalletsByClientId(UUID userId);

    @Query(" SELECT w FROM Wallet w WHERE w.id = :id ")
    Optional<Wallet> findWalletById(UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(" SELECT w FROM Wallet w WHERE w.id = :id ")
    Optional<Wallet> findLockedWalletById(UUID id);

    @Query("SELECT w FROM Wallet w WHERE w.walletType = WalletType.CLEARING")
    Optional<Wallet> findClearingWallet();

    @Query("SELECT w FROM Wallet w WHERE w.user.id = :userId AND w.user.role = UserRole.MERCHANT AND w.isActive = true")
    Optional<Wallet> findMainWalletByMerchantId(UUID userId);

    @Query("SELECT CASE WHEN COUNT(w) > 0 THEN true ELSE false END FROM Wallet w WHERE w.user.id = :userId AND w.isActive = true")
    boolean existsActiveWalletByUserId(UUID userId);
}