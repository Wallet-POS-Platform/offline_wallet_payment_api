package com.pixnetics.wallet_backend.payment.terminal;

import com.pixnetics.wallet_backend.payment.merchant.Merchant;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "terminals")
public class Terminal {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private Instant updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "merchant_id", nullable = false)
    private Merchant merchant;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "cert_id", nullable = false, unique = true)
    private String certId;

    @Column(name = "is_cert_active", nullable = false)
    private boolean isCertActive;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;
}
