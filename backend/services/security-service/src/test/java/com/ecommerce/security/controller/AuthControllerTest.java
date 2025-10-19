package com.ecommerce.security.controller;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
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
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "eureka.client.enabled=false"
})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Authentication Controller Integration Tests")
public class AuthControllerTest {

    @LocalServerPort
    private int port;

    private String baseUri;
    private String validToken;

    @BeforeEach
    void setUp() {
        baseUri = "http://localhost:" + port;
        RestAssured.baseURI = baseUri;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @Test
    @Order(1)
    @DisplayName("Should register a new user successfully")
    void testRegisterSuccess() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {
                    "email": "john.doe@example.com",
                    "password": "password123",
                    "firstName": "John",
                    "lastName": "Doe",
                    "phoneNumber": "+1234567890"
                }
                """)
        .when()
            .post("/auth/register")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("token", notNullValue())
            .body("type", equalTo("Bearer"))
            .body("expiresIn", notNullValue())
            .body("user.email", equalTo("john.doe@example.com"))
            .body("user.firstName", equalTo("John"))
            .body("user.lastName", equalTo("Doe"))
            .body("user.phoneNumber", equalTo("+1234567890"))
            .body("user.roles", hasItem("USER"))
            .body("issuedAt", notNullValue());
    }

    @Test
    @Order(2)
    @DisplayName("Should fail to register user with invalid email")
    void testRegisterInvalidEmail() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {
                    "email": "invalid-email",
                    "password": "password123",
                    "firstName": "John",
                    "lastName": "Doe"
                }
                """)
        .when()
            .post("/auth/register")
        .then()
            .statusCode(400);
    }

    @Test
    @Order(3)
    @DisplayName("Should fail to register user with short password")
    void testRegisterShortPassword() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {
                    "email": "user@example.com",
                    "password": "123",
                    "firstName": "John",
                    "lastName": "Doe"
                }
                """)
        .when()
            .post("/auth/register")
        .then()
            .statusCode(400);
    }

    @Test
    @Order(4)
    @DisplayName("Should fail to register user with missing required fields")
    void testRegisterMissingFields() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {
                    "email": "user@example.com",
                    "password": "password123"
                }
                """)
        .when()
            .post("/auth/register")
        .then()
            .statusCode(400);
    }

    @Test
    @Order(5)
    @DisplayName("Should fail to register duplicate email")
    void testRegisterDuplicateEmail() {
        // First registration
        given()
            .contentType(ContentType.JSON)
            .body("""
                {
                    "email": "duplicate@example.com",
                    "password": "password123",
                    "firstName": "Jane",
                    "lastName": "Doe"
                }
                """)
        .when()
            .post("/auth/register")
        .then()
            .statusCode(200);

        // Duplicate registration
        given()
            .contentType(ContentType.JSON)
            .body("""
                {
                    "email": "duplicate@example.com",
                    "password": "password456",
                    "firstName": "John",
                    "lastName": "Smith"
                }
                """)
        .when()
            .post("/auth/register")
        .then()
            .statusCode(400);
    }

    @Test
    @Order(6)
    @DisplayName("Should login successfully with valid credentials")
    void testLoginSuccess() {
        // First register a user
        given()
            .contentType(ContentType.JSON)
            .body("""
                {
                    "email": "login.test@example.com",
                    "password": "password123",
                    "firstName": "Login",
                    "lastName": "Test"
                }
                """)
        .when()
            .post("/auth/register")
        .then()
            .statusCode(200);

        // Then login
        validToken = given()
            .contentType(ContentType.JSON)
            .body("""
                {
                    "email": "login.test@example.com",
                    "password": "password123"
                }
                """)
        .when()
            .post("/auth/login")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("token", notNullValue())
            .body("type", equalTo("Bearer"))
            .body("user.email", equalTo("login.test@example.com"))
            .body("user.firstName", equalTo("Login"))
            .body("user.lastName", equalTo("Test"))
            .extract()
            .path("token");
    }

    @Test
    @Order(7)
    @DisplayName("Should fail login with invalid email")
    void testLoginInvalidEmail() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {
                    "email": "nonexistent@example.com",
                    "password": "password123"
                }
                """)
        .when()
            .post("/auth/login")
        .then()
            .statusCode(401);
    }

    @Test
    @Order(8)
    @DisplayName("Should fail login with wrong password")
    void testLoginWrongPassword() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {
                    "email": "login.test@example.com",
                    "password": "wrongpassword"
                }
                """)
        .when()
            .post("/auth/login")
        .then()
            .statusCode(401);
    }

    @Test
    @Order(9)
    @DisplayName("Should fail login with malformed request")
    void testLoginMalformedRequest() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {
                    "email": "",
                    "password": ""
                }
                """)
        .when()
            .post("/auth/login")
        .then()
            .statusCode(400);
    }



    @Test
    @Order(10)
    @DisplayName("Should refresh token successfully")
    void testRefreshTokenSuccess() {
        // Use token from previous tests
        if (validToken == null) {
            validToken = given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "email": "refresh@example.com",
                        "password": "password123",
                        "firstName": "Refresh",
                        "lastName": "Test"
                    }
                    """)
            .when()
                .post("/auth/register")
            .then()
                .extract()
                .path("token");
        }

        given()
            .header("Authorization", "Bearer " + validToken)
        .when()
            .post("/auth/refresh")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("token", notNullValue())
            .body("token", not(equalTo(validToken))) // Should be a new token
            .body("type", equalTo("Bearer"))
            .body("user", notNullValue());
    }

    @Test
    @Order(11)
    @DisplayName("Should fail to refresh with invalid token")
    void testRefreshTokenInvalid() {
        given()
            .header("Authorization", "Bearer invalid.token.here")
        .when()
            .post("/auth/refresh")
        .then()
            .statusCode(401);
    }

    @Test
    @Order(12)
    @DisplayName("Should handle malformed authorization header")
    void testMalformedAuthHeader() {
        given()
            .header("Authorization", "InvalidFormat")
        .when()
            .post("/auth/refresh")
        .then()
            .statusCode(400);
    }

    @Test
    @Order(13)
    @DisplayName("Should handle empty request body for login")
    void testLoginEmptyBody() {
        given()
            .contentType(ContentType.JSON)
            .body("{}")
        .when()
            .post("/auth/login")
        .then()
            .statusCode(400);
    }

    @Test
    @Order(14)
    @DisplayName("Should handle special characters in user data")
    void testRegisterWithSpecialCharacters() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {
                    "email": "special.chars+test@example.com",
                    "password": "P@ssw0rd!123",
                    "firstName": "João",
                    "lastName": "O'Connor-Smith",
                    "phoneNumber": "+1-800-123-4567"
                }
                """)
        .when()
            .post("/auth/register")
        .then()
            .statusCode(200)
            .body("user.email", equalTo("special.chars+test@example.com"))
            .body("user.firstName", equalTo("João"))
            .body("user.lastName", equalTo("O'Connor-Smith"))
            .body("user.phoneNumber", equalTo("+1-800-123-4567"));
    }

    @Test
    @Order(15)
    @DisplayName("Should handle long input values")
    void testRegisterWithLongValues() {
        String longEmail = "a".repeat(50) + "@example.com";
        String longFirstName = "A".repeat(100);
        String longLastName = "B".repeat(100);

        given()
            .contentType(ContentType.JSON)
            .body(String.format("""
                {
                    "email": "%s",
                    "password": "password123",
                    "firstName": "%s",
                    "lastName": "%s"
                }
                """, longEmail, longFirstName, longLastName))
        .when()
            .post("/auth/register")
        .then()
            .statusCode(anyOf(equalTo(200), equalTo(400))); // Depends on validation rules
    }
}