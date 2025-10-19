package com.ecommerce.security.exception;

/**
 * Custom exception for authentication-related errors (401 Unauthorized)
 */
public class AuthenticationException extends RuntimeException {
    
    public AuthenticationException(String message) {
        super(message);
    }
    
    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}