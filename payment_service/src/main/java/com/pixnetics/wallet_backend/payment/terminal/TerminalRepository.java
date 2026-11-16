package com.pixnetics.wallet_backend.payment.terminal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface TerminalRepository extends JpaRepository<Terminal, UUID> {

    @Query(" SELECT t FROM Terminal t WHERE t.merchant.id = :merchantId")
    List<Terminal> findAllByMerchantId(@Param("merchantId") UUID merchantId);
}