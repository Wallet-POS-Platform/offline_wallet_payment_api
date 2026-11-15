package com.pixnetics.wallet_backend.payment.client.exception;

public class PublicKeyAlreadyRegisteredException extends RuntimeException {

    public PublicKeyAlreadyRegisteredException(String username) {
        super("Client with username: " + username + " was not found");
    }
}
