package com.pixnetics.wallet_backend.payment.client;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface ClientRepository extends JpaRepository<Client, UUID> {

    @Query("select u from Client u where u.id = ?1 and u.user.role = UserRole.CLIENT")
    Optional<Client> findClientById(UUID id);
}