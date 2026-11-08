package com.pixnetics.wallet_backend.payment.merchant;

import com.pixnetics.wallet_backend.payment.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "merchants")
public class Merchant {

    @Id
    private UUID id;

    @MapsId
    @OneToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "id")
    private User user;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "category")
    private String category;

    @Column(name = "address")
    private String address;

    @Column(name = "payout_iban")
    private String payoutIBAN;

    @Column(name = "settle_type", nullable = false)
    private String settleType;
}
