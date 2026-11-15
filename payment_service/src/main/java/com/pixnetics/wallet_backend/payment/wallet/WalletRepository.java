package com.pixnetics.wallet_backend.payment.wallet;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WalletRepository extends JpaRepository<Wallet, UUID> {

    @Query("SELECT w FROM Wallet w WHERE w.user.id = :userId AND w.user.role = UserRole.CLIENT")
    List<Wallet> findAllWalletsByClientId(UUID userId);

    @Query("SELECT w FROM Wallet w WHERE w.user.id = :userId AND w.user.role = UserRole.MERCHANT AND w.isActive = true")
    Optional<Wallet> findMainWalletByMerchantId(UUID userId);

    @Query("SELECT CASE WHEN COUNT(w) > 0 THEN true ELSE false END FROM Wallet w WHERE w.user.id = :userId AND w.isActive = true")
    boolean existsActiveWalletByUserId(UUID userId);
}