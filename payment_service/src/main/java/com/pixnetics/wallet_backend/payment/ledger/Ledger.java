package com.pixnetics.wallet_backend.payment.ledger;

import com.pixnetics.wallet_backend.payment.transaction.Transaction;
import com.pixnetics.wallet_backend.payment.wallet.Wallet;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Entity
@Immutable
@Table(name = "ledger")
public class Ledger {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "trans_id", nullable = false, insertable=false, updatable=false)
    private UUID transId; //Read-Only

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trans_id", nullable = false)
    private Transaction transaction;

    @Column(name = "wallet_id", nullable = false, insertable = false, updatable = false)
    private UUID walletId; //Read-Only

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "vat", nullable = false, precision = 10, scale = 2)
    private BigDecimal vat = BigDecimal.ZERO;

    @Column(name = "vat_rate", nullable = false, precision = 4, scale = 2)
    private BigDecimal vatRate = BigDecimal.ZERO;

    @Column(name = "currency", nullable = false)
    private String currency = "EUR";
}
