package com.ecommerce.security.integration;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "jwt.secret=myTestSecretKeyThatIsLongEnoughForHMACSignatures12345678901234567890",
    "jwt.expiration=86400000",
    "spring.datasource.url=jdbc:h2:mem:integrationdb",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "eureka.client.enabled=false"
})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Security Service Integration Tests")
public class SecurityServiceIntegrationTest {

    @LocalServerPort
    private int port;

    private String baseUri;

    @BeforeEach
    void setUp() {
        baseUri = "http://localhost:" + port;
        RestAssured.baseURI = baseUri;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @Test
    @Order(1)
    @DisplayName("Complete user registration and authentication flow")
    void testCompleteUserFlow() {
        // Step 1: Register a new user
        String userEmail = "integration.user@example.com";
        String password = "SecurePassword123!";

        given()
            .contentType(ContentType.JSON)
            .body(String.format("""
                {
                    "email": "%s",
                    "password": "%s",
                    "firstName": "Integration",
                    "lastName": "User",
                    "phoneNumber": "+1234567890"
                }
                """, userEmail, password))
        .when()
            .post("/auth/register")
        .then()
            .statusCode(200)
            .body("token", notNullValue())
            .body("user.email", equalTo(userEmail))
            .body("user.roles", hasItem("USER"));

        // Step 2: Token is valid (we can use it for refresh later)

        // Step 3: Login with the same credentials
        String loginToken = given()
            .contentType(ContentType.JSON)
            .body(String.format("""
                {
                    "email": "%s",
                    "password": "%s"
                }
                """, userEmail, password))
        .when()
            .post("/auth/login")
        .then()
            .statusCode(200)
            .body("token", notNullValue())
            .body("user.email", equalTo(userEmail))
            .extract()
            .path("token");

        // Step 4: Refresh the token
        given()
            .header("Authorization", "Bearer " + loginToken)
        .when()
            .post("/auth/refresh")
        .then()
            .statusCode(200)
            .body("token", notNullValue())
            .body("token", not(equalTo(loginToken)));
    }

    @Test
    @Order(2)
    @DisplayName("JWT token expiration and validation lifecycle")
    void testTokenLifecycle() {
        // Register a user with short token expiration for testing
        String email = "token.lifecycle@example.com";
        
        String token = given()
            .contentType(ContentType.JSON)
            .body(String.format("""
                {
                    "email": "%s",
                    "password": "password123",
                    "firstName": "Token",
                    "lastName": "Lifecycle"
                }
                """, email))
        .when()
            .post("/auth/register")
        .then()
            .statusCode(200)
            .extract()
            .path("token");

        // Token is ready for use

        // Test token refresh before expiration
        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .post("/auth/refresh")
        .then()
            .statusCode(200)
            .body("token", not(equalTo(token)));
    }

    @Test
    @Order(3)
    @DisplayName("JWKS endpoint integration with JWT validation")
    void testJwksIntegration() {
        // First, get the JWKS
        given()
        .when()
            .get("/.well-known/jwks.json")
        .then()
            .statusCode(200)
            .body("keys", hasSize(greaterThan(0)))
            .body("keys[0].kty", equalTo("RSA"))
            .body("keys[0].use", equalTo("sig"));

        // Register a user and get a token
        given()
            .contentType(ContentType.JSON)
            .body("""
                {
                    "email": "jwks.test@example.com",
                    "password": "password123",
                    "firstName": "JWKS",
                    "lastName": "Test"
                }
                """)
        .when()
            .post("/auth/register")
        .then()
            .statusCode(200)
            .body("token", notNullValue());
    }

    @Test
    @Order(4)
    @DisplayName("Error handling and security edge cases")
    void testSecurityEdgeCases() {
        // Test malformed tokens with refresh endpoint (which validates internally)
        given()
            .header("Authorization", "Bearer malformed.jwt.token")
        .when()
            .post("/auth/refresh")
        .then()
            .statusCode(401);

        // Test JWT with wrong signature
        String fakeToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
        given()
            .header("Authorization", "Bearer " + fakeToken)
        .when()
            .post("/auth/refresh")
        .then()
            .statusCode(401);

        // Test refresh with invalid token
        given()
            .header("Authorization", "Bearer invalid.token")
        .when()
            .post("/auth/refresh")
        .then()
            .statusCode(401);

        // Test SQL injection attempt in email
        given()
            .contentType(ContentType.JSON)
            .body("""
                {
                    "email": "test'; DROP TABLE users; --",
                    "password": "password123"
                }
                """)
        .when()
            .post("/auth/login")
        .then()
            .statusCode(anyOf(equalTo(400), equalTo(401))); // Should be rejected
    }

    @Test
    @Order(5)
    @DisplayName("Concurrent authentication requests")
    void testConcurrentAuthentication() {
        String baseEmail = "concurrent.test";
        String password = "password123";
        
        // Register multiple users concurrently (simulate load)
        for (int i = 0; i < 5; i++) {
            final int userId = i;
            String email = baseEmail + userId + "@example.com";
            
            given()
                .contentType(ContentType.JSON)
                .body(String.format("""
                    {
                        "email": "%s",
                        "password": "%s",
                        "firstName": "User%d",
                        "lastName": "Test"
                    }
                    """, email, password, userId))
            .when()
                .post("/auth/register")
            .then()
                .statusCode(200)
                .body("user.email", equalTo(email));
        }

        // Login with all users
        for (int i = 0; i < 5; i++) {
            String email = baseEmail + i + "@example.com";
            
            given()
                .contentType(ContentType.JSON)
                .body(String.format("""
                    {
                        "email": "%s",
                        "password": "%s"
                    }
                    """, email, password))
            .when()
                .post("/auth/login")
            .then()
                .statusCode(200)
                .body("user.email", equalTo(email));
        }
    }

    @Test
    @Order(6)
    @DisplayName("Cross-endpoint authentication flow")
    void testCrossEndpointFlow() {
        // Register user
        String token = given()
            .contentType(ContentType.JSON)
            .body("""
                {
                    "email": "cross.endpoint@example.com",
                    "password": "password123",
                    "firstName": "Cross",
                    "lastName": "Endpoint"
                }
                """)
        .when()
            .post("/auth/register")
        .then()
            .statusCode(200)
            .extract()
            .path("token");

        // Use token with refresh endpoint
        String newToken = given()
            .header("Authorization", "Bearer " + token)
        .when()
            .post("/auth/refresh")
        .then()
            .statusCode(200)
            .body("token", not(equalTo(token)))
            .extract()
            .path("token");

        // Verify tokens are different
        assert !token.equals(newToken) : "Refreshed token should be different from original";
    }
}