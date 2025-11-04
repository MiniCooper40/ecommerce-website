package com.ecommerce.e2e.tests;

import com.ecommerce.e2e.config.E2ETestBase;
import com.ecommerce.e2e.util.AuthHelper;
import com.ecommerce.e2e.util.TestDataBuilder;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.*;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * End-to-End test for the shopping cart flow.
 * Tests the complete journey: Create product → Add to cart → View cart → Remove from cart
 */
@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CartFlowE2ETest extends E2ETestBase {

    private static String adminToken;
    private static String userToken;
    private static Long productId;
    private static Long cartItemId;
    private static String username;

    @BeforeAll
    static void setUp() {
        log.info("Setting up Cart Flow E2E Test");
        
        // Configure RestAssured
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        
        // Wait a moment for services to be fully ready
        log.info("Waiting for services to be fully ready...");
        try {
            Thread.sleep(5000); // Give services 5 seconds to settle after startup
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Get authentication tokens
        adminToken = AuthHelper.getAdminToken(GATEWAY_URL);
        username = TestDataBuilder.generateUsername();
        userToken = AuthHelper.getUserToken(GATEWAY_URL, username);
        
        log.info("Test setup complete. Admin and user tokens obtained.");
        log.info("Using Catalog URL: {}", CATALOG_URL);
        log.info("Using Cart URL: {}", CART_URL);
        log.info("Using Security URL: {}", SECURITY_URL);
    }

    @Test
    @Order(1)
    @DisplayName("Admin creates a product in catalog")
    void testCreateProduct() {
        log.info("TEST 1: Creating product in catalog service");
        
        Map<String, Object> productRequest = TestDataBuilder.createSimpleProduct(
                TestDataBuilder.generateProductName(),
                99.99
        );

        Response response = given()
                .baseUri(GATEWAY_URL)
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + adminToken)
                .body(productRequest)
            .when()
                .post("/api/catalog/products");
        
        log.info("Create product response status: {}", response.getStatusCode());
        log.info("Create product response body: {}", response.getBody().asString());
        
        response.then()
                .statusCode(200)  // Catalog service returns 200 OK, not 201 Created
                .body("name", equalTo(productRequest.get("name")))
                .body("price", equalTo(99.99f))
                .body("stockQuantity", equalTo(100))
                .body("id", notNullValue())
                .extract()
                .response();

        productId = response.jsonPath().getLong("id");
        log.info("Product created successfully with ID: {}", productId);
    }

    @Test
    @Order(2)
    @DisplayName("User adds product to cart")
    void testAddProductToCart() {
        log.info("TEST 2: Adding product {} to cart", productId);
        
        Map<String, Object> addToCartRequest = TestDataBuilder.createAddToCartRequest(productId, 2);

        Response response = given()
                .baseUri(GATEWAY_URL)
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + userToken)
                .body(addToCartRequest)
            .when()
                .post("/api/cart/items")
            .then()
                .statusCode(200)
                .body(notNullValue()) // Returns cartItemId (Long)
                .extract()
                .response();

        cartItemId = response.as(Long.class);
        log.info("Product added to cart successfully with cartItemId: {}", cartItemId);
    }

    @Test
    @Order(3)
    @DisplayName("User views cart with added product")
    void testViewCart() {
        log.info("TEST 3: Viewing cart contents");
        
        // Wait a moment for the cart view to be updated via events
        await()
                .atMost(10, SECONDS)
                .pollInterval(1, SECONDS)
                .untilAsserted(() -> {
                    given()
                            .baseUri(GATEWAY_URL)
                            .header("Authorization", "Bearer " + userToken)
                        .when()
                            .get("/api/cart")
                        .then()
                            .statusCode(200)
                            .body("items", hasSize(greaterThan(0)))
                            .body("items[0].productId", equalTo(productId.intValue()))
                            .body("items[0].quantity", equalTo(2))
                            .body("totalItems", equalTo(2))  // totalItems = sum of all quantities
                            .body("subtotal", notNullValue());
                });

        log.info("Cart viewed successfully with correct items");
    }

    @Test
    @Order(4)
    @DisplayName("User updates cart item quantity")
    void testUpdateCartItemQuantity() {
        log.info("TEST 4: Updating cart item quantity for cartItemId: {}", cartItemId);

        given()
                .baseUri(GATEWAY_URL)
                .header("Authorization", "Bearer " + userToken)
            .when()
                .put("/api/cart/items/" + cartItemId + "/quantity?quantity=5")
            .then()
                .statusCode(204);

        log.info("Cart item quantity updated successfully");
    }

    @Test
    @Order(5)
    @DisplayName("User verifies updated cart")
    void testVerifyUpdatedCart() {
        log.info("TEST 5: Verifying updated cart");
        
        // Wait for the cart view to reflect the update
        await()
                .atMost(10, SECONDS)
                .pollInterval(1, SECONDS)
                .untilAsserted(() -> {
                    given()
                            .baseUri(GATEWAY_URL)
                            .header("Authorization", "Bearer " + userToken)
                        .when()
                            .get("/api/cart")
                        .then()
                            .statusCode(200)
                            .body("items[0].quantity", equalTo(5))
                            .body("totalItems", equalTo(5));  // totalItems = sum of all quantities
                });

        log.info("Updated cart verified successfully");
    }

    @Test
    @Order(6)
    @DisplayName("User removes product from cart")
    void testRemoveProductFromCart() {
        log.info("TEST 6: Removing cart item with cartItemId: {}", cartItemId);

        given()
                .baseUri(GATEWAY_URL)
                .header("Authorization", "Bearer " + userToken)
            .when()
                .delete("/api/cart/items/" + cartItemId)
            .then()
                .statusCode(204);

        log.info("Product removed from cart successfully");
    }

    @Test
    @Order(7)
    @DisplayName("User verifies empty cart")
    void testVerifyEmptyCart() {
        log.info("TEST 7: Verifying cart is empty");
        
        // Wait for the cart view to reflect the removal
        await()
                .atMost(10, SECONDS)
                .pollInterval(1, SECONDS)
                .untilAsserted(() -> {
                    given()
                            .baseUri(GATEWAY_URL)
                            .header("Authorization", "Bearer " + userToken)
                        .when()
                            .get("/api/cart")
                        .then()
                            .statusCode(200)
                            .body("items", hasSize(0))
                            .body("totalItems", equalTo(0));
                });

        log.info("Empty cart verified successfully");
    }

    @AfterAll
    static void tearDown() {
        log.info("Cart Flow E2E Test completed");
        AuthHelper.clearCache();
    }
}
