package com.ecommerce.e2e.util;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for authentication in E2E tests.
 */
@Slf4j
public class AuthHelper {

    private static final Map<String, String> tokenCache = new HashMap<>();

    /**
     * Register a new user and return the JWT token.
     */
    public static String registerAndGetToken(String baseUrl, String username, String email, String password) {
        String cacheKey = username + ":" + email;
        
        // Return cached token if available
        if (tokenCache.containsKey(cacheKey)) {
            log.debug("Using cached token for user: {}", username);
            return tokenCache.get(cacheKey);
        }

        log.info("Registering new user: {}", username);
        
        Map<String, String> registerRequest = new HashMap<>();
        registerRequest.put("username", username);
        registerRequest.put("email", email);
        registerRequest.put("password", password);
        registerRequest.put("firstName", username.substring(0, 1).toUpperCase() + username.substring(1));
        registerRequest.put("lastName", "User");

        Response response = RestAssured
                .given()
                    .contentType(ContentType.JSON)
                    .body(registerRequest)
                .when()
                    .post(baseUrl + "/api/auth/register")
                .then()
                    .extract()
                    .response();

        // If registration fails with user already exists, try with a unique timestamp suffix
        if (response.getStatusCode() == 400 && 
            (response.getBody().asString().contains("already in use") || 
             response.getBody().asString().contains("already exists"))) {
            log.info("User already exists, trying with unique suffix: {}", username);
            String uniqueUsername = username + System.currentTimeMillis();
            String uniqueEmail = uniqueUsername + "@" + email.split("@")[1];
            return registerAndGetToken(baseUrl, uniqueUsername, uniqueEmail, password);
        }
        
        // Otherwise, expect 201 Created or 200 OK
        if (response.getStatusCode() != 201 && response.getStatusCode() != 200) {
            throw new RuntimeException("Registration failed with status: " + response.getStatusCode() + 
                                     ", body: " + response.getBody().asString());
        }

        String token = response.jsonPath().getString("token");
        tokenCache.put(cacheKey, token);
        
        log.info("User registered successfully: {}", username);
        return token;
    }

    /**
     * Login an existing user and return the JWT token.
     */
    public static String loginAndGetToken(String baseUrl, String username, String password) {
        return loginAndGetToken(baseUrl, username, username + "@ecommerce.com", password);
    }

    /**
     * Login an existing user and return the JWT token.
     */
    public static String loginAndGetToken(String baseUrl, String username, String email, String password) {
        String cacheKey = "login:" + username;
        
        // Return cached token if available
        if (tokenCache.containsKey(cacheKey)) {
            log.debug("Using cached token for user: {}", username);
            return tokenCache.get(cacheKey);
        }

        log.info("Logging in user: {}", username);
        
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", username);
        loginRequest.put("email", email);
        loginRequest.put("password", password);

        Response response = RestAssured
                .given()
                    .contentType(ContentType.JSON)
                    .body(loginRequest)
                .when()
                    .post(baseUrl + "/api/auth/login")
                .then()
                    .statusCode(200)
                    .extract()
                    .response();

        String token = response.jsonPath().getString("token");
        tokenCache.put(cacheKey, token);
        
        log.info("User logged in successfully: {}", username);
        return token;
    }

    /**
     * Get an admin token for privileged operations.
     */
    public static String getAdminToken(String baseUrl) {
        // Use the bootstrapped admin credentials from DataInitializationService
        return loginAndGetToken(baseUrl, "admin", "admin@ecommerce.com", "admin123");
    }

    /**
     * Get a regular user token.
     */
    public static String getUserToken(String baseUrl, String username) {
        return registerAndGetToken(baseUrl, username, username + "@test.com", "Test@123");
    }

    /**
     * Clear the token cache (useful between test classes).
     */
    public static void clearCache() {
        tokenCache.clear();
        log.debug("Token cache cleared");
    }
}
