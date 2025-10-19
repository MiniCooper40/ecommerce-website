package com.ecommerce.security.exception;

/**
 * Custom exception for validation errors (400 Bad Request)
 */
public class ValidationException extends RuntimeException {
    
    public ValidationException(String message) {
        super(message);
    }
    
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}