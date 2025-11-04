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
import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.*;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * End-to-End test for product update synchronization with cart.
 * Tests the event-driven CQRS pattern: Create product → Add to cart → Update product → Verify cart reflects changes
 * 
 * This test validates that when a product is updated in the catalog service:
 * 1. ProductUpdatedEvent is published to Kafka
 * 2. Cart service's ProductEventListener receives the event
 * 3. CartItemView records are updated with new product details
 * 4. Users see updated product information in their cart
 */
@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ProductUpdateCartSyncE2ETest extends E2ETestBase {

    private static String adminToken;
    private static String userToken;
    private static Long productId;
    private static Long cartItemId;
    private static String username;
    
    // Original product values
    private static final String ORIGINAL_NAME = "Original Product Name";
    private static final BigDecimal ORIGINAL_PRICE = new BigDecimal("99.99");
    
    // Updated product values
    private static final String UPDATED_NAME = "Updated Product Name";
    private static final BigDecimal UPDATED_PRICE = new BigDecimal("149.99");
    private static final String UPDATED_DESCRIPTION = "This product has been updated with new information";

    @BeforeAll
    static void setUp() {
        log.info("Setting up Product Update Cart Sync E2E Test");
        
        // Configure RestAssured
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        
        // Wait for services to be fully ready
        log.info("Waiting for services to be fully ready...");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Get authentication tokens
        adminToken = AuthHelper.getAdminToken(GATEWAY_URL);
        username = TestDataBuilder.generateUsername();
        userToken = AuthHelper.getUserToken(GATEWAY_URL, username);
        
        log.info("Test setup complete. Admin and user tokens obtained.");
        log.info("Using Gateway URL: {}", GATEWAY_URL);
    }

    @Test
    @Order(1)
    @DisplayName("Admin creates a product in catalog")
    void testCreateProduct() {
        log.info("TEST 1: Creating product in catalog service");
        
        Map<String, Object> productRequest = new HashMap<>();
        productRequest.put("name", ORIGINAL_NAME);
        productRequest.put("description", "Original product description");
        productRequest.put("price", ORIGINAL_PRICE);
        productRequest.put("category", "Electronics");
        productRequest.put("brand", "TestBrand");
        productRequest.put("stockQuantity", 100);
        productRequest.put("sku", "SKU-" + java.util.UUID.randomUUID().toString().substring(0, 8));

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
                .statusCode(200)
                .body("name", equalTo(ORIGINAL_NAME))
                .body("price", equalTo(ORIGINAL_PRICE.floatValue()))
                .body("stockQuantity", equalTo(100))
                .body("id", notNullValue())
                .extract()
                .response();

        productId = response.jsonPath().getLong("id");
        log.info("Product created successfully with ID: {} and price: {}", productId, ORIGINAL_PRICE);
    }

    @Test
    @Order(2)
    @DisplayName("User adds product to cart")
    void testAddProductToCart() {
        log.info("TEST 2: Adding product {} to cart", productId);
        
        Map<String, Object> addToCartRequest = TestDataBuilder.createAddToCartRequest(productId, 3);

        Response response = given()
                .baseUri(GATEWAY_URL)
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + userToken)
                .body(addToCartRequest)
            .when()
                .post("/api/cart/items")
            .then()
                .statusCode(200)
                .body(notNullValue())
                .extract()
                .response();

        cartItemId = response.as(Long.class);
        log.info("Product added to cart successfully with cartItemId: {}", cartItemId);
    }

    @Test
    @Order(3)
    @DisplayName("User views cart with original product details")
    void testViewCartWithOriginalProduct() {
        log.info("TEST 3: Viewing cart to verify original product details");
        
        // Wait for cart view to be updated via Kafka events
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
                            .body("items[0].productName", equalTo(ORIGINAL_NAME))
                            .body("items[0].productPrice", equalTo(ORIGINAL_PRICE.floatValue()))
                            .body("items[0].quantity", equalTo(3));
                });

        log.info("Cart verified with original product name: '{}' and price: {}", ORIGINAL_NAME, ORIGINAL_PRICE);
    }

    @Test
    @Order(4)
    @DisplayName("Admin updates product details")
    void testUpdateProduct() {
        log.info("TEST 4: Updating product {} with new name and price", productId);
        
        Map<String, Object> updateRequest = new HashMap<>();
        updateRequest.put("name", UPDATED_NAME);
        updateRequest.put("description", UPDATED_DESCRIPTION);
        updateRequest.put("price", UPDATED_PRICE);
        updateRequest.put("category", "Electronics");
        updateRequest.put("brand", "TestBrand");
        updateRequest.put("stockQuantity", 100);

        Response response = given()
                .baseUri(GATEWAY_URL)
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + adminToken)
                .body(updateRequest)
            .when()
                .put("/api/catalog/products/" + productId);
        
        log.info("Update product response status: {}", response.getStatusCode());
        log.info("Update product response body: {}", response.getBody().asString());
        
        response.then()
                .statusCode(200)
                .body("name", equalTo(UPDATED_NAME))
                .body("price", equalTo(UPDATED_PRICE.floatValue()))
                .body("description", equalTo(UPDATED_DESCRIPTION))
                .extract()
                .response();

        log.info("Product updated successfully - Name: '{}', Price: {}", UPDATED_NAME, UPDATED_PRICE);
    }

    @Test
    @Order(5)
    @DisplayName("User views cart and sees updated product details")
    void testViewCartWithUpdatedProduct() {
        log.info("TEST 5: Verifying cart shows updated product details via event synchronization");
        
        // Wait for ProductUpdatedEvent to be processed by cart service
        // This tests the event-driven architecture:
        // 1. Catalog service publishes ProductUpdatedEvent
        // 2. Cart service ProductEventListener receives event
        // 3. CartItemViewRepository updates all cart items with new product details
        await()
                .atMost(15, SECONDS)
                .pollInterval(1, SECONDS)
                .untilAsserted(() -> {
                    Response cartResponse = given()
                            .baseUri(GATEWAY_URL)
                            .header("Authorization", "Bearer " + userToken)
                        .when()
                            .get("/api/cart")
                        .then()
                            .statusCode(200)
                            .extract()
                            .response();
                    
                    log.info("Cart response: {}", cartResponse.getBody().asString());
                    
                    cartResponse.then()
                            .body("items", hasSize(1))
                            .body("items[0].productId", equalTo(productId.intValue()))
                            .body("items[0].productName", equalTo(UPDATED_NAME))
                            .body("items[0].productPrice", equalTo(UPDATED_PRICE.floatValue()))
                            .body("items[0].quantity", equalTo(3))
                            .body("totalItems", equalTo(3))
                            .body("subtotal", notNullValue());
                });

        log.info("✅ Cart successfully synchronized with updated product details!");
        log.info("   Product name updated: '{}' → '{}'", ORIGINAL_NAME, UPDATED_NAME);
        log.info("   Product price updated: {} → {}", ORIGINAL_PRICE, UPDATED_PRICE);
        log.info("   Event-driven CQRS architecture working correctly!");
    }

    @Test
    @Order(6)
    @DisplayName("Verify cart subtotal reflects updated price")
    void testVerifyCartSubtotal() {
        log.info("TEST 6: Verifying cart subtotal calculation with updated price");
        
        // Expected subtotal = quantity (3) × updated price (149.99)
        BigDecimal expectedSubtotal = UPDATED_PRICE.multiply(new BigDecimal("3"));
        
        given()
                .baseUri(GATEWAY_URL)
                .header("Authorization", "Bearer " + userToken)
            .when()
                .get("/api/cart")
            .then()
                .statusCode(200)
                .body("items[0].productPrice", equalTo(UPDATED_PRICE.floatValue()))
                .body("subtotal", equalTo(expectedSubtotal.floatValue()));

        log.info("Cart subtotal correctly calculated: {} (3 × {})", expectedSubtotal, UPDATED_PRICE);
    }

    @Test
    @Order(7)
    @DisplayName("User removes product from cart")
    void testCleanupCart() {
        log.info("TEST 7: Cleaning up - removing product from cart");

        given()
                .baseUri(GATEWAY_URL)
                .header("Authorization", "Bearer " + userToken)
            .when()
                .delete("/api/cart/items/" + cartItemId)
            .then()
                .statusCode(204);

        log.info("Product removed from cart successfully");
    }

    @AfterAll
    static void tearDown() {
        log.info("Product Update Cart Sync E2E Test completed");
        log.info("This test validated the event-driven synchronization between catalog and cart services");
        AuthHelper.clearCache();
    }
}
