package com.pixnetics.wallet_backend.payment.client;

import com.pixnetics.wallet_backend.payment.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "clients")
public class Client {

    @Id
    private UUID id;

    @MapsId
    @OneToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "id")
    private User user;

    @Column(name = "public_key", nullable = false, unique = true)
    private byte[] publicKey;

    @Column(name = "key_status", nullable = false)
    private String keyStatus;
}
