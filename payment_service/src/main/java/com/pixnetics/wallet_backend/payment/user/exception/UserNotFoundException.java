package com.pixnetics.wallet_backend.payment.user.exception;

import java.util.UUID;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(UUID id) {
        super("Client with id: " + id + " was not found");
    }

    public UserNotFoundException(String username) {
        super("Client with username: " + username + " was not found");
    }
}
