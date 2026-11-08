package com.pixnetics.wallet_backend.payment.transaction;

import com.pixnetics.wallet_backend.enums.TransactionStatus;
import com.pixnetics.wallet_backend.payment.wallet.Wallet;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false, insertable = false)
    private Instant updatedAt;

    @Column(name = "type", nullable = false)
    private String type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TransactionStatus status;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "from_wallet_id", referencedColumnName = "id")
    private Wallet fromWalletId;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "to_wallet_id", referencedColumnName = "id")
    private Wallet toWalletId;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "vat_rate", nullable = false, precision = 4, scale = 2)
    private BigDecimal vatRate = BigDecimal.ZERO;

    @Column(name = "currency", nullable = false)
    private String currency = "EUR";

    @Column(name = "failure_reason")
    private String failureReason;
}
