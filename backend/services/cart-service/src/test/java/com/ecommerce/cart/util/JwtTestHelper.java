package com.ecommerce.cart.util;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Utility class for generating test JWT tokens.
 */
public class JwtTestHelper {

    private static final String DEFAULT_TOKEN_VALUE = "test-token";
    private static final Instant DEFAULT_ISSUED_AT = Instant.now();
    private static final Instant DEFAULT_EXPIRES_AT = Instant.now().plusSeconds(3600);

    /**
     * Generate a test JWT with user ID, email, and roles.
     */
    public static String generateTestToken(String userId, String email, String... roles) {
        return DEFAULT_TOKEN_VALUE;
    }

    /**
     * Generate a mock Jwt object for testing.
     */
    public static Jwt generateMockJwt(String userId, String email, String... roles) {
        Map<String, Object> headers = new HashMap<>();
        headers.put("alg", "RS256");
        headers.put("typ", "JWT");

        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", userId);
        claims.put("userId", userId);
        claims.put("email", email);
        claims.put("roles", Arrays.asList(roles));
        claims.put("firstName", "Test");
        claims.put("lastName", "User");

        return new Jwt(
            DEFAULT_TOKEN_VALUE,
            DEFAULT_ISSUED_AT,
            DEFAULT_EXPIRES_AT,
            headers,
            claims
        );
    }
}
