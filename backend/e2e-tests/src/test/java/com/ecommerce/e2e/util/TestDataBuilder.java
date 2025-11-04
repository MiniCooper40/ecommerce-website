package com.ecommerce.e2e.util;

import java.math.BigDecimal;
import java.util.*;

/**
 * Builder for creating test product data.
 */
public class TestDataBuilder {

    /**
     * Create a product creation request.
     */
    public static Map<String, Object> createProductRequest(String name, String description, BigDecimal price, Integer stock) {
        Map<String, Object> product = new HashMap<>();
        product.put("name", name);
        product.put("description", description);
        product.put("price", price);
        product.put("stockQuantity", stock);
        product.put("category", "Electronics");
        product.put("brand", "TestBrand");
        product.put("sku", "SKU-" + UUID.randomUUID().toString().substring(0, 8));
        product.put("images", Arrays.asList(
                createImageDto("https://example.com/image1.jpg", true, 0),
                createImageDto("https://example.com/image2.jpg", false, 1)
        ));
        return product;
    }

    /**
     * Create a simple product for testing.
     */
    public static Map<String, Object> createSimpleProduct(String name, Double price) {
        Map<String, Object> product = new HashMap<>();
        product.put("name", name);
        product.put("description", "Test product: " + name);
        product.put("price", price);
        product.put("stockQuantity", 100);
        product.put("category", "Electronics");
        product.put("brand", "TestBrand");
        product.put("sku", "SKU-" + UUID.randomUUID().toString().substring(0, 8));
        // Don't include images for simple E2E tests
        return product;
    }

    /**
     * Create an add-to-cart request.
     */
    public static Map<String, Object> createAddToCartRequest(Long productId, Integer quantity) {
        Map<String, Object> request = new HashMap<>();
        request.put("productId", productId);
        request.put("quantity", quantity);
        return request;
    }

    /**
     * Create an update cart item request.
     */
    public static Map<String, Object> createUpdateCartItemRequest(Integer quantity) {
        Map<String, Object> request = new HashMap<>();
        request.put("quantity", quantity);
        return request;
    }

    /**
     * Create a checkout request.
     */
    public static Map<String, Object> createCheckoutRequest(String shippingAddress, String paymentMethod) {
        Map<String, Object> request = new HashMap<>();
        request.put("shippingAddress", shippingAddress);
        request.put("paymentMethod", paymentMethod);
        return request;
    }

    /**
     * Create an image DTO.
     */
    private static Map<String, Object> createImageDto(String url, boolean isPrimary, int displayOrder) {
        Map<String, Object> image = new HashMap<>();
        image.put("url", url);
        image.put("isPrimary", isPrimary);
        image.put("displayOrder", displayOrder);
        return image;
    }

    /**
     * Generate a unique username for testing.
     */
    public static String generateUsername() {
        return "user_" + UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * Generate a unique email for testing.
     */
    public static String generateEmail() {
        return generateUsername() + "@test.com";
    }

    /**
     * Generate a unique product name.
     */
    public static String generateProductName() {
        return "Product_" + UUID.randomUUID().toString().substring(0, 8);
    }
}
