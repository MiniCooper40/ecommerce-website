package com.ecommerce.security.service;

import com.ecommerce.security.dto.AuthResponse;
import com.ecommerce.security.dto.LoginRequest;
import com.ecommerce.security.dto.RegisterRequest;

/**
 * Interface for authentication service operations
 */
public interface AuthService {
    
    /**
     * Register a new user
     */
    AuthResponse register(RegisterRequest request);
    
    /**
     * Login a user
     */
    AuthResponse login(LoginRequest request);
    
    /**
     * Refresh a JWT token
     */
    AuthResponse refresh(String authHeader);
    
    /**
     * Get current user information by user ID
     */
    AuthResponse.UserDto getCurrentUser(Long userId);
}