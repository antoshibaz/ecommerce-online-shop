package com.kotomka.core.exceptions;

import org.springframework.security.core.AuthenticationException;

public class AuthException extends AuthenticationException {

    public AuthException(String message) {
        super(message);
    }
}