package com.ecommerce.e2e.tests;

import static io.restassured.RestAssured.given;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.ecommerce.e2e.config.E2ETestBase;
import com.ecommerce.e2e.util.AuthHelper;
import com.ecommerce.e2e.util.TestDataBuilder;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;

/**
 * End-to-End test for order validation flow.
 * Tests the complete journey:
 * 1. Admin creates products with limited stock
 * 2. User adds products to cart
 * 3. User tries to checkout with invalid cart (not enough stock)
 * 4. User modifies cart to become valid
 * 5. User checks out successfully
 * 6. Verify order is validated asynchronously (cart and stock validation)
 */
@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class OrderValidationE2ETest extends E2ETestBase {

    private static String adminToken;
    private static String userToken;
    private static String username;
    
    // Product IDs
    private static Long product1Id; // Limited stock: 5 units
    private static Long product2Id; // Good stock: 100 units
    
    // Cart item IDs
    private static Long cartItem1Id;
    private static Long cartItem2Id;
    
    // Order IDs
    private static Long invalidOrderId;
    private static Long validOrderId;

    @BeforeAll
    static void setUp() {
        log.info("=".repeat(80));
        log.info("Setting up Order Validation E2E Test");
        log.info("=".repeat(80));
        
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
        log.info("Testing user: {}", username);
    }

    @Test
    @Order(1)
    @DisplayName("Admin creates Product 1 with limited stock (5 units)")
    void test01_CreateProduct1WithLimitedStock() {
        log.info("\n" + "=".repeat(80));
        log.info("TEST 1: Admin creates Product 1 with limited stock");
        log.info("=".repeat(80));
        
        Map<String, Object> productRequest = TestDataBuilder.createSimpleProduct(
                "Limited Stock Product",
                49.99
        );
        productRequest.put("stockQuantity", 5); // Only 5 units available

        Response response = given()
                .baseUri(GATEWAY_URL)
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + adminToken)
                .body(productRequest)
            .when()
                .post("/api/catalog/products")
            .then()
                .statusCode(200)
                .body("name", equalTo("Limited Stock Product"))
                .body("price", equalTo(49.99f))
                .body("stockQuantity", equalTo(5))
                .body("id", notNullValue())
                .extract()
                .response();

        product1Id = response.jsonPath().getLong("id");
        log.info("✓ Product 1 created successfully with ID: {} (Stock: 5 units)", product1Id);
    }

    @Test
    @Order(2)
    @DisplayName("Admin creates Product 2 with good stock (100 units)")
    void test02_CreateProduct2WithGoodStock() {
        log.info("\n" + "=".repeat(80));
        log.info("TEST 2: Admin creates Product 2 with good stock");
        log.info("=".repeat(80));
        
        Map<String, Object> productRequest = TestDataBuilder.createSimpleProduct(
                "Good Stock Product",
                99.99
        );
        productRequest.put("stockQuantity", 100);

        Response response = given()
                .baseUri(GATEWAY_URL)
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + adminToken)
                .body(productRequest)
            .when()
                .post("/api/catalog/products")
            .then()
                .statusCode(200)
                .body("name", equalTo("Good Stock Product"))
                .body("price", equalTo(99.99f))
                .body("stockQuantity", equalTo(100))
                .body("id", notNullValue())
                .extract()
                .response();

        product2Id = response.jsonPath().getLong("id");
        log.info("✓ Product 2 created successfully with ID: {} (Stock: 100 units)", product2Id);
    }

    @Test
    @Order(3)
    @DisplayName("User adds Product 1 to cart with excessive quantity (10 units, but only 5 available)")
    void test03_AddProduct1ToCartWithExcessiveQuantity() {
        log.info("\n" + "=".repeat(80));
        log.info("TEST 3: User adds Product 1 to cart with excessive quantity");
        log.info("=".repeat(80));
        
        Map<String, Object> addToCartRequest = TestDataBuilder.createAddToCartRequest(product1Id, 10);

        Response response = given()
                .baseUri(GATEWAY_URL)
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + userToken)
                .body(addToCartRequest)
            .when()
                .post("/api/cart/items")
            .then()
                .statusCode(200)
                .extract()
                .response();

        cartItem1Id = response.as(Long.class);
        log.info("✓ Product 1 added to cart with ID: {} (Requested: 10, Available: 5)", cartItem1Id);
    }

    @Test
    @Order(4)
    @DisplayName("User adds Product 2 to cart with valid quantity (2 units)")
    void test04_AddProduct2ToCartWithValidQuantity() {
        log.info("\n" + "=".repeat(80));
        log.info("TEST 4: User adds Product 2 to cart with valid quantity");
        log.info("=".repeat(80));
        
        Map<String, Object> addToCartRequest = TestDataBuilder.createAddToCartRequest(product2Id, 2);

        Response response = given()
                .baseUri(GATEWAY_URL)
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + userToken)
                .body(addToCartRequest)
            .when()
                .post("/api/cart/items")
            .then()
                .statusCode(200)
                .extract()
                .response();

        cartItem2Id = response.as(Long.class);
        log.info("✓ Product 2 added to cart with ID: {}", cartItem2Id);
    }

    @Test
    @Order(5)
    @DisplayName("User verifies cart has both products")
    void test05_VerifyCartHasBothProducts() {
        log.info("\n" + "=".repeat(80));
        log.info("TEST 5: User verifies cart has both products");
        log.info("=".repeat(80));
        
        await()
                .atMost(15, SECONDS)
                .pollInterval(1, SECONDS)
                .untilAsserted(() -> {
                    Response response = given()
                            .baseUri(GATEWAY_URL)
                            .header("Authorization", "Bearer " + userToken)
                        .when()
                            .get("/api/cart")
                        .then()
                            .statusCode(200)
                            .body("items", hasSize(2))
                            .body("totalItems", equalTo(12)) // 10 + 2
                            .extract()
                            .response();
                    
                    log.info("Cart contents: {}", response.jsonPath().prettify());
                });

        log.info("✓ Cart verified: 2 items, total quantity: 12");
    }

    @Test
    @Order(6)
    @DisplayName("User attempts to create order with invalid cart (insufficient stock)")
    void test06_CreateOrderWithInvalidCart() {
        log.info("\n" + "=".repeat(80));
        log.info("TEST 6: User attempts to create order (will fail stock validation)");
        log.info("=".repeat(80));
        
        List<Map<String, Object>> orderItems = Arrays.asList(
                TestDataBuilder.createOrderItem(product1Id, 10), // Exceeds available stock
                TestDataBuilder.createOrderItem(product2Id, 2)
        );
        
        Map<String, Object> orderRequest = TestDataBuilder.createOrderRequest(orderItems);

        Response response = given()
                .baseUri(GATEWAY_URL)
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + userToken)
                .body(orderRequest)
            .when()
                .post("/api/orders")
            .then()
                .statusCode(200)
                .body("id", notNullValue())
                .body("status", equalTo("PENDING"))
                .body("cartValidated", equalTo(false))
                .body("stockValidated", equalTo(false))
                .extract()
                .response();

        invalidOrderId = response.jsonPath().getLong("id");
        log.info("✓ Order created with ID: {} (Status: PENDING, awaiting validation)", invalidOrderId);
        log.info("Order details: {}", response.jsonPath().prettify());
    }

    @Test
    @Order(7)
    @DisplayName("Wait and verify order is CANCELLED due to insufficient stock")
    void test07_VerifyOrderCancelledDueToInsufficientStock() {
        log.info("\n" + "=".repeat(80));
        log.info("TEST 7: Waiting for async validation to complete (should CANCEL)");
        log.info("=".repeat(80));
        
        await()
                .atMost(30, SECONDS)
                .pollInterval(2, SECONDS)
                .untilAsserted(() -> {
                    Response response = given()
                            .baseUri(GATEWAY_URL)
                            .header("Authorization", "Bearer " + userToken)
                        .when()
                            .get("/api/orders/" + invalidOrderId)
                        .then()
                            .statusCode(200)
                            .extract()
                            .response();
                    
                    String status = response.jsonPath().getString("status");
                    Boolean stockValidated = response.jsonPath().getBoolean("stockValidated");
                    
                    log.info("Order {} validation status - Status: {}, StockValidated: {}", 
                            invalidOrderId, status, stockValidated);
                    
                    // Verify the order was cancelled due to validation failure
                    response.then()
                            .body("status", equalTo("CANCELLED"))
                            .body("stockValidated", equalTo(false))
                            .body("validationCompletedAt", notNullValue());
                });

        log.info("✓ Order {} correctly CANCELLED due to insufficient stock", invalidOrderId);
    }

    @Test
    @Order(8)
    @DisplayName("User updates cart to reduce Product 1 quantity to valid amount (3 units)")
    void test08_UpdateCartToValidQuantity() {
        log.info("\n" + "=".repeat(80));
        log.info("TEST 8: User updates cart to reduce quantity to valid amount");
        log.info("=".repeat(80));
        
        given()
                .baseUri(GATEWAY_URL)
                .header("Authorization", "Bearer " + userToken)
            .when()
                .put("/api/cart/items/" + cartItem1Id + "/quantity?quantity=3")
            .then()
                .statusCode(204);

        log.info("✓ Cart updated: Product 1 quantity reduced from 10 to 3");
    }

    @Test
    @Order(9)
    @DisplayName("User verifies updated cart")
    void test09_VerifyUpdatedCart() {
        log.info("\n" + "=".repeat(80));
        log.info("TEST 9: User verifies updated cart");
        log.info("=".repeat(80));
        
        await()
                .atMost(20, SECONDS)
                .pollInterval(1, SECONDS)
                .untilAsserted(() -> {
                    Response response = given()
                            .baseUri(GATEWAY_URL)
                            .header("Authorization", "Bearer " + userToken)
                        .when()
                            .get("/api/cart")
                        .then()
                            .statusCode(200)
                            .body("items", hasSize(2))
                            .body("totalItems", equalTo(5)) // 3 + 2
                            .extract()
                            .response();
                    
                    // Verify Product 1 has quantity 3
                    List<Map<String, Object>> items = response.jsonPath().getList("items");
                    boolean foundProduct1 = items.stream()
                            .anyMatch(item -> 
                                    item.get("productId").equals(product1Id.intValue()) && 
                                    item.get("quantity").equals(3));
                    
                    Assertions.assertTrue(foundProduct1, "Product 1 should have quantity 3");
                    
                    log.info("Cart contents: {}", response.jsonPath().prettify());
                });

        log.info("✓ Cart verified: Product 1 quantity is now 3 (valid)");
        
        // Add a small delay to ensure eventual consistency of the cart view
        // The CartItemView is updated asynchronously via events, and even though
        // the GET /cart endpoint works, there might be a brief inconsistency window
        try {
            log.info("Waiting 2 seconds for cart view eventual consistency...");
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Test
    @Order(10)
    @DisplayName("User creates order with valid cart")
    void test10_CreateOrderWithValidCart() {
        log.info("\n" + "=".repeat(80));
        log.info("TEST 10: User creates order with valid cart");
        log.info("=".repeat(80));
        
        // BEFORE creating the order, verify cart state one more time
        Response cartCheck = given()
                .baseUri(GATEWAY_URL)
                .header("Authorization", "Bearer " + userToken)
            .when()
                .get("/api/cart");
        
        log.info("FINAL CART CHECK before Order 2:");
        log.info("Cart: {}", cartCheck.then().extract().asString());
        
        List<Map<String, Object>> orderItems = Arrays.asList(
                TestDataBuilder.createOrderItem(product1Id, 3), // Within available stock
                TestDataBuilder.createOrderItem(product2Id, 2)
        );
        
        log.info("Order will request: Product {} (qty 3), Product {} (qty 2)", product1Id, product2Id);
        
        Map<String, Object> orderRequest = TestDataBuilder.createOrderRequest(orderItems);

        Response response = given()
                .baseUri(GATEWAY_URL)
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + userToken)
                .body(orderRequest)
            .when()
                .post("/api/orders")
            .then()
                .statusCode(200)
                .body("id", notNullValue())
                .body("status", equalTo("PENDING"))
                .body("cartValidated", equalTo(false))
                .body("stockValidated", equalTo(false))
                .extract()
                .response();

        validOrderId = response.jsonPath().getLong("id");
        log.info("✓ Order created with ID: {} (Status: PENDING, awaiting validation)", validOrderId);
        log.info("Order details: {}", response.jsonPath().prettify());
    }

    @Test
    @Order(11)
    @DisplayName("Wait and verify order is CONFIRMED after successful validation")
    void test11_VerifyOrderConfirmedAfterSuccessfulValidation() {
        log.info("\n" + "=".repeat(80));
        log.info("TEST 11: Waiting for async validation to complete (should CONFIRM)");
        log.info("=".repeat(80));
        
        await()
                .atMost(30, SECONDS)
                .pollInterval(2, SECONDS)
                .untilAsserted(() -> {
                    Response response = given()
                            .baseUri(GATEWAY_URL)
                            .header("Authorization", "Bearer " + userToken)
                        .when()
                            .get("/api/orders/" + validOrderId)
                        .then()
                            .statusCode(200)
                            .extract()
                            .response();
                    
                    String status = response.jsonPath().getString("status");
                    Boolean cartValidated = response.jsonPath().getBoolean("cartValidated");
                    Boolean stockValidated = response.jsonPath().getBoolean("stockValidated");
                    
                    log.info("Order {} validation status - Status: {}, CartValidated: {}, StockValidated: {}", 
                            validOrderId, status, cartValidated, stockValidated);
                    
                    // Verify the order was confirmed after successful validation
                    response.then()
                            .body("status", equalTo("CONFIRMED"))
                            .body("cartValidated", equalTo(true))
                            .body("stockValidated", equalTo(true))
                            .body("validationCompletedAt", notNullValue());
                    
                    log.info("Final order details: {}", response.jsonPath().prettify());
                });

        log.info("✓ Order {} successfully CONFIRMED after validation", validOrderId);
    }

    @Test
    @Order(12)
    @DisplayName("Verify both orders exist with correct final states")
    void test12_VerifyBothOrdersExist() {
        log.info("\n" + "=".repeat(80));
        log.info("TEST 12: Verify both orders exist with correct states");
        log.info("=".repeat(80));
        
        Response response = given()
                .baseUri(GATEWAY_URL)
                .header("Authorization", "Bearer " + userToken)
            .when()
                .get("/api/orders")
            .then()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(2))
                .extract()
                .response();

        List<Map<String, Object>> orders = response.jsonPath().getList("$");
        
        log.info("User has {} total orders", orders.size());
        
        // Find and verify invalid order
        Map<String, Object> invalidOrder = orders.stream()
                .filter(o -> o.get("id").equals(invalidOrderId.intValue()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Invalid order not found"));
        
        Assertions.assertEquals("CANCELLED", invalidOrder.get("status"), 
                "Invalid order should be CANCELLED");
        log.info("✓ Order {} is CANCELLED (as expected)", invalidOrderId);
        
        // Find and verify valid order
        Map<String, Object> validOrder = orders.stream()
                .filter(o -> o.get("id").equals(validOrderId.intValue()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Valid order not found"));
        
        Assertions.assertEquals("CONFIRMED", validOrder.get("status"), 
                "Valid order should be CONFIRMED");
        log.info("✓ Order {} is CONFIRMED (as expected)", validOrderId);
        
        log.info("✓ Both orders verified with correct final states");
    }

    @AfterAll
    static void tearDown() {
        log.info("\n" + "=".repeat(80));
        log.info("Order Validation E2E Test completed");
        log.info("Summary:");
        log.info("  - Created 2 products (1 with limited stock, 1 with good stock)");
        log.info("  - Added products to cart with invalid quantity");
        log.info("  - Order {} created and CANCELLED due to insufficient stock", invalidOrderId);
        log.info("  - Cart updated to valid quantities");
        log.info("  - Order {} created and CONFIRMED after successful validation", validOrderId);
        log.info("=".repeat(80));
        AuthHelper.clearCache();
    }
}
