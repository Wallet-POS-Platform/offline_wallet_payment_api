package com.pixnetics.wallet_backend.payment.client;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface ClientRepository extends JpaRepository<Client, UUID> {
    boolean existsByPublicKey(byte[] publicKey);
}