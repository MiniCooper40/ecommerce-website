package com.ecommerce.cart.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.ecommerce.cart.dto.CartSummaryDto;
import com.ecommerce.cart.entity.CartItemView;
import com.ecommerce.cart.repository.CartItemViewRepository;
import com.ecommerce.shared.testutil.BaseTest;

@DisplayName("Cart Query Service Tests")
public class CartQueryServiceTest extends BaseTest {

    @Mock
    private CartItemViewRepository cartItemViewRepository;

    @InjectMocks
    private CartQueryService cartQueryService;

    private String testUserId;

    @BeforeEach
    void setUp() {
        testUserId = "user123";
    }

    @Test
    @DisplayName("getCart - should return cart summary with items")
    public void testGetCart_WithItems() {
        // Arrange
        List<CartItemView> cartItems = createMockCartItemViews();
        when(cartItemViewRepository.findByUserId(testUserId)).thenReturn(cartItems);

        // Act
        CartSummaryDto result = cartQueryService.getCart(testUserId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getItems().size());
        assertEquals(3, result.getTotalItems()); // 1 + 2
        assertEquals(new BigDecimal("89.97"), result.getSubtotal()); // 29.99 * 1 + 29.99 * 2

        // Verify items
        assertEquals(100L, result.getItems().get(0).getProductId());
        assertEquals("Test Product 1", result.getItems().get(0).getProductName());
        assertEquals(1, result.getItems().get(0).getQuantity());

        assertEquals(101L, result.getItems().get(1).getProductId());
        assertEquals("Test Product 2", result.getItems().get(1).getProductName());
        assertEquals(2, result.getItems().get(1).getQuantity());

        verify(cartItemViewRepository, times(1)).findByUserId(testUserId);
    }

    @Test
    @DisplayName("getCart - should return empty cart when no items")
    public void testGetCart_EmptyCart() {
        // Arrange
        when(cartItemViewRepository.findByUserId(testUserId))
                .thenReturn(Collections.emptyList());

        // Act
        CartSummaryDto result = cartQueryService.getCart(testUserId);

        // Assert
        assertNotNull(result);
        assertTrue(result.getItems().isEmpty());
        assertEquals(0, result.getTotalItems());
        assertEquals(BigDecimal.ZERO, result.getSubtotal());
        assertEquals(BigDecimal.ZERO, result.getTotal());

        verify(cartItemViewRepository, times(1)).findByUserId(testUserId);
    }

    @Test
    @DisplayName("getCart - should correctly map all item properties")
    public void testGetCart_ItemMapping() {
        // Arrange
        CartItemView view = CartItemView.builder()
                .id(1L)
                .cartItemId(10L)
                .cartId("cart-user123")
                .userId(testUserId)
                .productId(100L)
                .productName("Detailed Product")
                .productPrice(new BigDecimal("49.99"))
                .productImageUrl("http://example.com/image.jpg")
                .quantity(3)
                .available(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(cartItemViewRepository.findByUserId(testUserId))
                .thenReturn(Arrays.asList(view));

        // Act
        CartSummaryDto result = cartQueryService.getCart(testUserId);

        // Assert
        assertEquals(1, result.getItems().size());
        var item = result.getItems().get(0);
        
        assertEquals(10L, item.getId());
        assertEquals(100L, item.getProductId());
        assertEquals("Detailed Product", item.getProductName());
        assertEquals(new BigDecimal("49.99"), item.getProductPrice());
        assertEquals("http://example.com/image.jpg", item.getProductImageUrl());
        assertEquals(3, item.getQuantity());
        assertTrue(item.getAvailable());
        assertNotNull(item.getCreatedAt());
        assertNotNull(item.getUpdatedAt());
    }

    @Test
    @DisplayName("getCart - should calculate correct subtotals for items")
    public void testGetCart_SubtotalCalculation() {
        // Arrange
        CartItemView view1 = createCartItemView(1L, 100L, "Product 1", new BigDecimal("10.00"), 2);
        CartItemView view2 = createCartItemView(2L, 101L, "Product 2", new BigDecimal("25.50"), 3);
        
        when(cartItemViewRepository.findByUserId(testUserId))
                .thenReturn(Arrays.asList(view1, view2));

        // Act
        CartSummaryDto result = cartQueryService.getCart(testUserId);

        // Assert
        assertEquals(new BigDecimal("20.00"), result.getItems().get(0).getSubtotal()); // 10.00 * 2
        assertEquals(new BigDecimal("76.50"), result.getItems().get(1).getSubtotal()); // 25.50 * 3
        assertEquals(new BigDecimal("96.50"), result.getSubtotal()); // 20.00 + 76.50
    }

    @Test
    @DisplayName("getCartItemCount - should return total item count")
    public void testGetCartItemCount_WithItems() {
        // Arrange
        when(cartItemViewRepository.sumQuantityByUserId(testUserId)).thenReturn(5);

        // Act
        Integer result = cartQueryService.getCartItemCount(testUserId);

        // Assert
        assertEquals(5, result);
        verify(cartItemViewRepository, times(1)).sumQuantityByUserId(testUserId);
    }

    @Test
    @DisplayName("getCartItemCount - should return zero when cart is empty")
    public void testGetCartItemCount_EmptyCart() {
        // Arrange
        when(cartItemViewRepository.sumQuantityByUserId(testUserId)).thenReturn(null);

        // Act
        Integer result = cartQueryService.getCartItemCount(testUserId);

        // Assert
        assertEquals(0, result);
        verify(cartItemViewRepository, times(1)).sumQuantityByUserId(testUserId);
    }

    @Test
    @DisplayName("getCartItemCount - should handle zero count")
    public void testGetCartItemCount_ZeroCount() {
        // Arrange
        when(cartItemViewRepository.sumQuantityByUserId(testUserId)).thenReturn(0);

        // Act
        Integer result = cartQueryService.getCartItemCount(testUserId);

        // Assert
        assertEquals(0, result);
    }

    @Test
    @DisplayName("getCart - should handle unavailable products")
    public void testGetCart_UnavailableProducts() {
        // Arrange
        CartItemView availableItem = createCartItemView(1L, 100L, "Available", new BigDecimal("10.00"), 1);
        availableItem.setAvailable(true);
        
        CartItemView unavailableItem = createCartItemView(2L, 101L, "Unavailable", new BigDecimal("20.00"), 1);
        unavailableItem.setAvailable(false);
        
        when(cartItemViewRepository.findByUserId(testUserId))
                .thenReturn(Arrays.asList(availableItem, unavailableItem));

        // Act
        CartSummaryDto result = cartQueryService.getCart(testUserId);

        // Assert
        assertEquals(2, result.getItems().size());
        assertTrue(result.getItems().get(0).getAvailable());
        assertEquals(false, result.getItems().get(1).getAvailable());
    }

    // Helper methods

    private List<CartItemView> createMockCartItemViews() {
        CartItemView view1 = createCartItemView(1L, 100L, "Test Product 1", new BigDecimal("29.99"), 1);
        CartItemView view2 = createCartItemView(2L, 101L, "Test Product 2", new BigDecimal("29.99"), 2);
        return Arrays.asList(view1, view2);
    }

    private CartItemView createCartItemView(Long cartItemId, Long productId, String productName, 
                                            BigDecimal price, Integer quantity) {
        return CartItemView.builder()
                .id(cartItemId)
                .cartItemId(cartItemId)
                .cartId("cart-" + testUserId)
                .userId(testUserId)
                .productId(productId)
                .productName(productName)
                .productPrice(price)
                .productImageUrl("http://example.com/image" + productId + ".jpg")
                .quantity(quantity)
                .available(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
