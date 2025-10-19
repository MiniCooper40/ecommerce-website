package com.ecommerce.security.exception;

/**
 * Custom exception for user not found errors (401 Unauthorized)
 */
public class UserNotFoundException extends RuntimeException {
    
    public UserNotFoundException(String message) {
        super(message);
    }
    
    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}