package com.ecommerce.security.util;

import static io.restassured.RestAssured.given;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
    "jwt.expiration=5000", // 5 seconds for faster expiration testing
    "spring.datasource.url=jdbc:h2:mem:jwtutildb",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "eureka.client.enabled=false"
})
@DisplayName("JWT Utility Security Tests")
public class JwtUtilityTest {

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
    @DisplayName("Should generate JWT tokens with proper structure")
    void testJwtTokenStructure() {
        // Register user and get token
        String token = given()
            .contentType(ContentType.JSON)
            .body("""
                {
                    "email": "jwt.structure@example.com",
                    "password": "password123",
                    "firstName": "JWT",
                    "lastName": "Structure"
                }
                """)
        .when()
            .post("/auth/register")
        .then()
            .statusCode(200)
            .extract()
            .path("token");

        // JWT should have 3 parts separated by dots
        String[] parts = token.split("\\.");
        assert parts.length == 3 : "JWT should have exactly 3 parts (header.payload.signature)";
        
        // Each part should be base64 encoded (non-empty)
        for (String part : parts) {
            assert !part.isEmpty() : "JWT parts should not be empty";
        }
    }

    @Test
    @DisplayName("Should generate tokens that can be refreshed after short expiration")
    void testJwtExpiration() {
        // Register user with short expiration
        String token = given()
            .contentType(ContentType.JSON)
            .body("""
                {
                    "email": "jwt.expiration@example.com",
                    "password": "password123",
                    "firstName": "JWT",
                    "lastName": "Expiration"
                }
                """)
        .when()
            .post("/auth/register")
        .then()
            .statusCode(200)
            .extract()
            .path("token");

        // Token should work for refresh immediately
        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .post("/auth/refresh")
        .then()
            .statusCode(200);

        // Wait for token to expire (5 seconds + buffer)
        try {
            Thread.sleep(6000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Expired token should fail refresh
        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .post("/auth/refresh")
        .then()
            .statusCode(401);
    }

    @Test
    @DisplayName("Should detect JWT token tampering via refresh endpoint")
    void testJwtTampering() {
        // Get a valid token
        String validToken = given()
            .contentType(ContentType.JSON)
            .body("""
                {
                    "email": "jwt.tampering@example.com",
                    "password": "password123",
                    "firstName": "JWT",
                    "lastName": "Tampering"
                }
                """)
        .when()
            .post("/auth/register")
        .then()
            .statusCode(200)
            .extract()
            .path("token");

        // Tamper with the token by changing a character
        String tamperedToken = validToken.substring(0, validToken.length() - 5) + "XXXXX";

        // Tampered token should be rejected by refresh
        given()
            .header("Authorization", "Bearer " + tamperedToken)
        .when()
            .post("/auth/refresh")
        .then()
            .statusCode(401);
    }

    @Test
    @DisplayName("Should handle various malformed JWT formats via refresh endpoint")
    void testMalformedJwtHandling() {
        // Test various malformed JWTs with refresh endpoint
        String[] malformedTokens = {
            "invalid",
            "invalid.token", 
            "invalid.token.signature.extra",
            "...",
            "a.b.",
            ".b.c",
            "a..c"
        };

        for (String malformedToken : malformedTokens) {
            given()
                .header("Authorization", "Bearer " + malformedToken)
            .when()
                .post("/auth/refresh")
            .then()
                .statusCode(401);
        }
    }

    @Test
    @DisplayName("Should generate unique tokens for different users")
    void testTokenUniqueness() {
        // Register first user
        String token1 = given()
            .contentType(ContentType.JSON)
            .body("""
                {
                    "email": "unique1@example.com",
                    "password": "password123",
                    "firstName": "User",
                    "lastName": "One"
                }
                """)
        .when()
            .post("/auth/register")
        .then()
            .statusCode(200)
            .extract()
            .path("token");

        // Register second user
        String token2 = given()
            .contentType(ContentType.JSON)
            .body("""
                {
                    "email": "unique2@example.com",
                    "password": "password123",
                    "firstName": "User",
                    "lastName": "Two"
                }
                """)
        .when()
            .post("/auth/register")
        .then()
            .statusCode(200)
            .extract()
            .path("token");

        // Tokens should be different
        assert !token1.equals(token2) : "Tokens for different users should be unique";

        // Both tokens should work for refresh
        given()
            .header("Authorization", "Bearer " + token1)
        .when()
            .post("/auth/refresh")
        .then()
            .statusCode(200);

        given()
            .header("Authorization", "Bearer " + token2)
        .when()
            .post("/auth/refresh")
        .then()
            .statusCode(200);
    }

    @Test
    @DisplayName("Should generate different tokens on refresh")
    void testTokenRefreshUniqueness() {
        // Get initial token
        String originalToken = given()
            .contentType(ContentType.JSON)
            .body("""
                {
                    "email": "refresh.unique@example.com",
                    "password": "password123",
                    "firstName": "Refresh",
                    "lastName": "Unique"
                }
                """)
        .when()
            .post("/auth/register")
        .then()
            .statusCode(200)
            .extract()
            .path("token");

        // Refresh token
        String refreshedToken = given()
            .header("Authorization", "Bearer " + originalToken)
        .when()
            .post("/auth/refresh")
        .then()
            .statusCode(200)
            .extract()
            .path("token");

        // Tokens should be different
        assert !originalToken.equals(refreshedToken) : "Refreshed token should be different from original";

        // Refreshed token should work for another refresh
        given()
            .header("Authorization", "Bearer " + refreshedToken)
        .when()
            .post("/auth/refresh")
        .then()
            .statusCode(200);
    }

    @Test
    @DisplayName("Should handle authorization header validation via refresh endpoint")
    void testAuthHeaderValidation() {
        // Invalid Bearer formats should be rejected by refresh
        given()
            .header("Authorization", "Bearer invalid.token.here")
        .when()
            .post("/auth/refresh")
        .then()
            .statusCode(401);

        // Non-Bearer format should be rejected
        given()
            .header("Authorization", "some.jwt.token")
        .when()
            .post("/auth/refresh")
        .then()
            .statusCode(400);
    }

    @Test
    @DisplayName("Should maintain token consistency across login sessions")
    void testLoginTokenConsistency() {
        String email = "consistency@example.com";
        String password = "password123";

        // Register user
        given()
            .contentType(ContentType.JSON)
            .body(String.format("""
                {
                    "email": "%s",
                    "password": "%s",
                    "firstName": "Consistency",
                    "lastName": "Test"
                }
                """, email, password))
        .when()
            .post("/auth/register")
        .then()
            .statusCode(200);

        // Login multiple times
        String token1 = given()
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
            .extract()
            .path("token");

        String token2 = given()
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
            .extract()
            .path("token");

        // Each login should generate a new token (for security)
        assert !token1.equals(token2) : "Each login should generate a fresh token";

        // Both tokens should work for refresh
        given()
            .header("Authorization", "Bearer " + token1)
        .when()
            .post("/auth/refresh")
        .then()
            .statusCode(200);

        given()
            .header("Authorization", "Bearer " + token2)
        .when()
            .post("/auth/refresh")
        .then()
            .statusCode(200);
    }
}