package com.ecommerce.security.config;

import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Custom BearerTokenResolver that extracts JWT from cookies or Authorization header.
 * 
 * This resolver provides dual-mode JWT extraction:
 * 1. First attempts to extract from Authorization header (standard OAuth2 approach)
 * 2. Falls back to extracting from cookies (for cookie-based auth)
 * 
 * This allows services to accept JWT tokens from both sources, providing flexibility
 * for different client types (SPAs with cookies, mobile apps with headers, etc.)
 * 
 * Automatically available to all services that include security-lib dependency.
 */
@Component
public class CookieBearerTokenResolver implements BearerTokenResolver {

    private static final String COOKIE_NAME = "jwt_token";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    public String resolve(HttpServletRequest request) {
        // First, try to get token from Authorization header (standard approach)
        String authHeader = request.getHeader(AUTHORIZATION_HEADER);
        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            return authHeader.substring(BEARER_PREFIX.length());
        }

        // If not in header, try to get from cookies
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (COOKIE_NAME.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        // No token found
        return null;
    }
}
