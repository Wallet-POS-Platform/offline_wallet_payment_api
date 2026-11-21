package com.pixnetics.wallet_backend.common.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

@Getter
public class AuthenticatedUser extends User {

    private final UUID id;

    public AuthenticatedUser(UUID id,
                             String username,
                             String passwordHash,
                             boolean enabled,
                             Collection<? extends GrantedAuthority> authorities) {

        super(username, passwordHash, enabled, true, true, true, authorities);
        this.id = id;
    }
}
