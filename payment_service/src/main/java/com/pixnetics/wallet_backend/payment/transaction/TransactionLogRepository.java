package com.pixnetics.wallet_backend.payment.transaction;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TransactionLogRepository extends JpaRepository<TransactionLog, UUID> {}