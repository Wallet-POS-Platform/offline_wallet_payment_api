package com.pixnetics.wallet_backend.payment.transaction;

import com.pixnetics.wallet_backend.enums.TransactionStatus;
import com.pixnetics.wallet_backend.payment.user.User;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Immutable;

import java.time.Instant;
import java.util.UUID;

@Data
@Entity
@Immutable
@Table(name = "transaction_logs")
public class TransactionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "timestamp", nullable = false, insertable = false, updatable = false)
    private Instant timestamp;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "transaction_id", referencedColumnName = "id")
    private Transaction transactionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_status")
    private TransactionStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", nullable = false)
    private TransactionStatus toStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by", referencedColumnName = "id")
    private User changedBy;

    @Column(name = "reason")
    private String reason;
}