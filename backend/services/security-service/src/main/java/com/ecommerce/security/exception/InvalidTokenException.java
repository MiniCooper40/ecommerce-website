package com.ecommerce.security.exception;

/**
 * Custom exception for JWT token-related errors (401 Unauthorized)
 */
public class InvalidTokenException extends RuntimeException {
    
    public InvalidTokenException(String message) {
        super(message);
    }
    
    public InvalidTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}