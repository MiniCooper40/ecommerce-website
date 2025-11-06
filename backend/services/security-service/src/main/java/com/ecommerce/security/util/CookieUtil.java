package com.ecommerce.security.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Utility class for managing JWT cookies
 */
@Component
public class CookieUtil {

    @Value("${jwt.cookie.name}")
    private String cookieName;

    @Value("${jwt.cookie.path}")
    private String cookiePath;

    @Value("${jwt.cookie.http-only}")
    private boolean httpOnly;

    @Value("${jwt.cookie.secure}")
    private boolean secure;

    @Value("${jwt.cookie.same-site}")
    private String sameSite;

    @Value("${jwt.cookie.max-age}")
    private int maxAge;

    /**
     * Create a JWT cookie with the given token
     */
    public ResponseCookie createJwtCookie(String token) {
        return ResponseCookie.from(cookieName, token)
                .path(cookiePath)
                .httpOnly(httpOnly)
                .secure(secure)
                .sameSite(sameSite)
                .maxAge(maxAge)
                .build();
    }

    /**
     * Create a cookie for clearing/deleting the JWT
     */
    public ResponseCookie createDeleteCookie() {
        return ResponseCookie.from(cookieName, "")
                .path(cookiePath)
                .httpOnly(httpOnly)
                .secure(secure)
                .sameSite(sameSite)
                .maxAge(0)
                .build();
    }

    /**
     * Extract JWT token from request cookies
     */
    public String getJwtFromCookies(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookieName.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    /**
     * Get the configured cookie name
     */
    public String getCookieName() {
        return cookieName;
    }
}
