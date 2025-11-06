package com.ecommerce.security.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.security.dto.AuthResponse;
import com.ecommerce.security.dto.LoginRequest;
import com.ecommerce.security.dto.RegisterRequest;
import com.ecommerce.security.service.AuthService;
import com.ecommerce.security.util.CookieUtil;
import com.ecommerce.security.util.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private CookieUtil cookieUtil;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        ResponseCookie cookie = cookieUtil.createJwtCookie(response.getToken());
        
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        ResponseCookie cookie = cookieUtil.createJwtCookie(response.getToken());
        
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @CookieValue(value = "jwt_token", required = false) String cookieToken,
            HttpServletRequest request) {
        
        // Try to get token from cookie first, then fall back to Authorization header
        String token = cookieToken;
        if (token == null && authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        } else if (token == null) {
            token = cookieUtil.getJwtFromCookies(request);
        }
        
        AuthResponse response = authService.refresh(token != null ? "Bearer " + token : null);
        ResponseCookie cookie = cookieUtil.createJwtCookie(response.getToken());
        
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        ResponseCookie cookie = cookieUtil.createDeleteCookie();
        
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .build();
    }

    @GetMapping("/me")
    public ResponseEntity<AuthResponse.UserDto> getCurrentUser(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @CookieValue(value = "jwt_token", required = false) String cookieToken,
            HttpServletRequest request) {
        
        // Try to get token from cookie first, then fall back to Authorization header
        String token = cookieToken;
        if (token == null && authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        } else if (token == null) {
            token = cookieUtil.getJwtFromCookies(request);
        }
        
        if (token == null || !jwtUtil.validateToken(token)) {
            return ResponseEntity.status(401).build();
        }
        
        // Extract user ID from token
        String userId = jwtUtil.getUserIdFromToken(token);
        
        // Get user details
        AuthResponse.UserDto user = authService.getCurrentUser(Long.parseLong(userId));
        
        return ResponseEntity.ok(user);
    }
}