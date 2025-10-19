package com.ecommerce.security.controller;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.notNullValue;

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
    "jwt.expiration=86400000",
    "spring.datasource.url=jdbc:h2:mem:jwksdb",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "eureka.client.enabled=false"
})
@DisplayName("JWKS Controller Integration Tests")
public class JwksControllerTest {

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
    @DisplayName("Should return JWKS with valid structure")
    void testGetJwksSuccess() {
        given()
        .when()
            .get("/.well-known/jwks.json")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("keys", notNullValue())
            .body("keys", hasSize(greaterThan(0)))
            .body("keys[0]", hasKey("kty"))
            .body("keys[0]", hasKey("kid"))
            .body("keys[0]", hasKey("use"))
            .body("keys[0]", hasKey("alg"))
            .body("keys[0]", hasKey("n"))
            .body("keys[0]", hasKey("e"))
            .body("keys[0].kty", equalTo("RSA"))
            .body("keys[0].use", equalTo("sig"))
            .body("keys[0].alg", equalTo("RS256"));
    }

    @Test
    @DisplayName("Should return consistent JWKS across multiple requests")
    void testJwksConsistency() {
        String firstResponse = 
            given()
            .when()
                .get("/.well-known/jwks.json")
            .then()
                .statusCode(200)
                .extract()
                .asString();

        String secondResponse = 
            given()
            .when()
                .get("/.well-known/jwks.json")
            .then()
                .statusCode(200)
                .extract()
                .asString();

        // JWKS should be consistent across requests (same keys)
        assert firstResponse.equals(secondResponse) : "JWKS responses should be consistent";
    }

    @Test
    @DisplayName("Should have proper CORS headers for JWKS endpoint")
    void testJwksCorsHeaders() {
        given()
            .header("Origin", "https://example.com")
        .when()
            .get("/.well-known/jwks.json")
        .then()
            .statusCode(200)
            .header("Access-Control-Allow-Origin", notNullValue());
    }

    @Test
    @DisplayName("Should handle HEAD request to JWKS endpoint")
    void testJwksHeadRequest() {
        given()
        .when()
            .head("/.well-known/jwks.json")
        .then()
            .statusCode(200)
            .header("Content-Type", containsString("application/json"));
    }

    @Test
    @DisplayName("Should return valid JSON structure for JWKS")
    void testJwksJsonStructure() {
        given()
        .when()
            .get("/.well-known/jwks.json")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("keys", isA(java.util.List.class))
            .body("keys[0].kty", isA(String.class))
            .body("keys[0].kid", isA(String.class))
            .body("keys[0].use", isA(String.class))
            .body("keys[0].alg", isA(String.class))
            .body("keys[0].n", isA(String.class))
            .body("keys[0].e", isA(String.class));
    }

    @Test
    @DisplayName("Should have required RSA key components")
    void testRsaKeyComponents() {
        given()
        .when()
            .get("/.well-known/jwks.json")
        .then()
            .statusCode(200)
            .body("keys[0].n", matchesPattern("^[A-Za-z0-9_-]+$")) // Base64URL encoded modulus
            .body("keys[0].e", matchesPattern("^[A-Za-z0-9_-]+$")) // Base64URL encoded exponent
            .body("keys[0].n.length()", greaterThan(300)) // RSA modulus should be substantial
            .body("keys[0].e", anyOf(equalTo("AQAB"), equalTo("AAEAAQ"))); // Common RSA exponents
    }

    @Test
    @DisplayName("Should support OPTIONS request for JWKS endpoint")
    void testJwksOptionsRequest() {
        given()
        .when()
            .options("/.well-known/jwks.json")
        .then()
            .statusCode(anyOf(equalTo(200), equalTo(204)));
    }
}