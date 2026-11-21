package com.pixnetics.wallet_backend;

import com.pixnetics.wallet_backend.payment.client.exception.ClientNotFoundException;
import com.pixnetics.wallet_backend.payment.client.exception.PublicKeyAlreadyRegisteredException;
import com.pixnetics.wallet_backend.payment.merchant.exception.MerchantNotFoundException;
import com.pixnetics.wallet_backend.payment.transaction.exception.TransactionException;
import com.pixnetics.wallet_backend.payment.user.exception.UsernameAlreadyExistsException;
import com.pixnetics.wallet_backend.payment.wallet.exception.WalletAccessDeniedException;
import com.pixnetics.wallet_backend.payment.wallet.exception.WalletCreationException;
import com.pixnetics.wallet_backend.payment.wallet.exception.WalletInactiveException;
import com.pixnetics.wallet_backend.payment.wallet.exception.WalletNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.springframework.http.converter.HttpMessageNotReadableException;

import java.util.*;
import java.util.stream.Collectors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // --------------------- Custom Exceptions -----------------------//
    @ExceptionHandler(ClientNotFoundException.class)
    public ProblemDetail handleException(ClientNotFoundException e) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                e.getMessage()
        );
    }

    @ExceptionHandler(MerchantNotFoundException.class)
    public ProblemDetail handleException(MerchantNotFoundException e) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                e.getMessage()
        );
    }

    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public ProblemDetail handleException(UsernameAlreadyExistsException e) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                e.getMessage()
        );
    }

    @ExceptionHandler(PublicKeyAlreadyRegisteredException.class)
    public ProblemDetail handleException(PublicKeyAlreadyRegisteredException e) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                e.getMessage()
        );
    }

    @ExceptionHandler(WalletCreationException.class)
    public ProblemDetail handleException(WalletCreationException e) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                e.getMessage()
        );
    }

    @ExceptionHandler(WalletNotFoundException.class)
    public ProblemDetail handleException(WalletNotFoundException e) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                "There is an issue with respective Wallet"
        );
    }

    @ExceptionHandler(WalletInactiveException.class)
    public ProblemDetail handleException(WalletInactiveException e) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                e.getMessage()
        );
    }

    @ExceptionHandler(WalletAccessDeniedException.class)
    public ProblemDetail handleWalletAccessDenied(WalletAccessDeniedException e) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.FORBIDDEN,
                e.getMessage()
        );
    }

    @ExceptionHandler(TransactionException.class)
    public ProblemDetail handleException(TransactionException e) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                e.getMessage()
        );
    }
    // --------------------- In Built Exceptions -----------------------//
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleException(HttpMessageNotReadableException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Request body contains invalid values."
        );

        Map<String, List<String>> errors = new HashMap<>();

        Throwable cause = e.getMostSpecificCause();
        log.error("Http Message error occurred", cause);

        if (cause instanceof InvalidFormatException invalidFormatException) {

            String fieldName = invalidFormatException.getPath()
                    .stream()
                    .map(JsonMappingException.Reference::getFieldName)
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining("."));

            String message;

            if (invalidFormatException.getTargetType().equals(UUID.class)) {
                message = "Invalid UUID format. Expected a valid 36-character UUID.";
            } else {
                message = "Invalid value for the provided field.";
            }

            errors.computeIfAbsent(fieldName, key -> new ArrayList<>()).add(message);

        } else {
            errors.put("request", List.of("Malformed JSON or invalid field value."));
        }

        problem.setTitle("Invalid Request Body");
        problem.setProperty("errors", errors);
        return problem;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleException(MethodArgumentNotValidException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "One or more fields have invalid values."
        );

        Map<String, List<String>> errors = new HashMap<>();

        e.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        errors.computeIfAbsent(
                                error.getField(),
                                key -> new ArrayList<>()
                        ).add(error.getDefaultMessage())
                );


        problem.setTitle("Validation Failed");
        problem.setProperty("errors", errors);
        return problem;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleException(Exception e) {
        log.error("Unexpected error occurred: {}", e.getMessage(), e);

        return ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred."
        );
    }
}