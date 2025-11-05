package com.ecommerce.cart.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.ecommerce.cart.entity.CartItem;
import com.ecommerce.cart.repository.CartItemRepository;
import com.ecommerce.shared.events.EventPublisher;
import com.ecommerce.shared.events.domain.CartItemAddedEvent;
import com.ecommerce.shared.events.domain.CartItemRemovedEvent;
import com.ecommerce.shared.events.domain.CartItemUpdatedEvent;
import com.ecommerce.shared.testutil.BaseTest;

@DisplayName("Cart Command Service Tests")
public class CartCommandServiceTest extends BaseTest {

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private CartCommandService cartCommandService;

    private String testUserId;
    private Long testProductId;

    @BeforeEach
    void setUp() {
        testUserId = "user123";
        testProductId = 100L;
    }

    @Test
    @DisplayName("addItemToCart - should create new cart item when product not in cart")
    public void testAddItemToCart_NewItem() {
        // Arrange
        when(cartItemRepository.findByUserIdAndProductId(testUserId, testProductId))
                .thenReturn(Optional.empty());

        CartItem savedItem = createCartItem(1L, testUserId, testProductId, 2);
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(savedItem);

        // Act
        Long result = cartCommandService.addItemToCart(testUserId, testProductId, 2);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result);

        ArgumentCaptor<CartItem> cartItemCaptor = ArgumentCaptor.forClass(CartItem.class);
        verify(cartItemRepository, times(1)).save(cartItemCaptor.capture());

        CartItem capturedItem = cartItemCaptor.getValue();
        assertEquals(testUserId, capturedItem.getUserId());
        assertEquals(testProductId, capturedItem.getProductId());
        assertEquals(2, capturedItem.getQuantity());

        verify(eventPublisher, times(1)).publish(any(CartItemAddedEvent.class));
    }

    @Test
    @DisplayName("addItemToCart - should update quantity when product already in cart")
    public void testAddItemToCart_ExistingItem() {
        // Arrange
        CartItem existingItem = createCartItem(1L, testUserId, testProductId, 2);
        when(cartItemRepository.findByUserIdAndProductId(testUserId, testProductId))
                .thenReturn(Optional.of(existingItem));

        CartItem updatedItem = createCartItem(1L, testUserId, testProductId, 5);
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(updatedItem);

        // Act
        Long result = cartCommandService.addItemToCart(testUserId, testProductId, 3);

        // Assert
        assertEquals(1L, result);
        assertEquals(5, existingItem.getQuantity()); // 2 + 3 = 5

        verify(cartItemRepository, times(1)).save(existingItem);
        verify(eventPublisher, times(1)).publish(any(CartItemUpdatedEvent.class));
    }

    @Test
    @DisplayName("updateItemQuantity - should update quantity successfully")
    public void testUpdateItemQuantity_Success() {
        // Arrange
        Long itemId = 1L;
        CartItem existingItem = createCartItem(itemId, testUserId, testProductId, 2);
        when(cartItemRepository.findByUserIdAndId(testUserId, itemId))
                .thenReturn(Optional.of(existingItem));
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(existingItem);

        // Act
        cartCommandService.updateItemQuantity(testUserId, itemId, 5);

        // Assert
        assertEquals(5, existingItem.getQuantity());
        verify(cartItemRepository, times(1)).save(existingItem);
        verify(eventPublisher, times(1)).publish(any(CartItemUpdatedEvent.class));
    }

    @Test
    @DisplayName("updateItemQuantity - should throw exception when item not found")
    public void testUpdateItemQuantity_ItemNotFound() {
        // Arrange
        Long itemId = 999L;
        when(cartItemRepository.findByUserIdAndId(testUserId, itemId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            cartCommandService.updateItemQuantity(testUserId, itemId, 5);
        });
    }

    @Test
    @DisplayName("updateItemQuantity - should remove item when quantity is zero")
    public void testUpdateItemQuantity_ZeroQuantity() {
        // Arrange
        Long itemId = 1L;
        CartItem existingItem = createCartItem(itemId, testUserId, testProductId, 2);
        when(cartItemRepository.findByUserIdAndId(testUserId, itemId))
                .thenReturn(Optional.of(existingItem));

        // Act
        cartCommandService.updateItemQuantity(testUserId, itemId, 0);

        // Assert
        verify(cartItemRepository, times(1)).delete(existingItem);
        verify(eventPublisher, times(1)).publish(any(CartItemRemovedEvent.class));
    }

    @Test
    @DisplayName("updateItemQuantity - should remove item when quantity is negative")
    public void testUpdateItemQuantity_NegativeQuantity() {
        // Arrange
        Long itemId = 1L;
        CartItem existingItem = createCartItem(itemId, testUserId, testProductId, 2);
        when(cartItemRepository.findByUserIdAndId(testUserId, itemId))
                .thenReturn(Optional.of(existingItem));

        // Act
        cartCommandService.updateItemQuantity(testUserId, itemId, -1);

        // Assert
        verify(cartItemRepository, times(1)).delete(existingItem);
        verify(eventPublisher, times(1)).publish(any(CartItemRemovedEvent.class));
    }

    @Test
    @DisplayName("removeItemFromCart - should remove item successfully")
    public void testRemoveItemFromCart_Success() {
        // Arrange
        Long itemId = 1L;
        CartItem existingItem = createCartItem(itemId, testUserId, testProductId, 2);
        when(cartItemRepository.findByUserIdAndId(testUserId, itemId))
                .thenReturn(Optional.of(existingItem));

        // Act
        cartCommandService.removeItemFromCart(testUserId, itemId);

        // Assert
        verify(cartItemRepository, times(1)).delete(existingItem);
        verify(eventPublisher, times(1)).publish(any(CartItemRemovedEvent.class));
    }

    @Test
    @DisplayName("removeItemFromCart - should throw exception when item not found")
    public void testRemoveItemFromCart_ItemNotFound() {
        // Arrange
        Long itemId = 999L;
        when(cartItemRepository.findByUserIdAndId(testUserId, itemId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            cartCommandService.removeItemFromCart(testUserId, itemId);
        });
    }

    @Test
    @DisplayName("clearCart - should remove all items for user")
    public void testClearCart_Success() {
        // Arrange
        CartItem item1 = createCartItem(1L, testUserId, 100L, 2);
        CartItem item2 = createCartItem(2L, testUserId, 101L, 1);
        when(cartItemRepository.findByUserId(testUserId))
                .thenReturn(Arrays.asList(item1, item2));

        // Act
        cartCommandService.clearCart(testUserId);

        // Assert
        verify(cartItemRepository, times(1)).deleteByUserId(testUserId);
        verify(eventPublisher, times(2)).publish(any(CartItemRemovedEvent.class));
    }

    @Test
    @DisplayName("clearCart - should handle empty cart gracefully")
    public void testClearCart_EmptyCart() {
        // Arrange
        when(cartItemRepository.findByUserId(testUserId))
                .thenReturn(Arrays.asList());

        // Act
        cartCommandService.clearCart(testUserId);

        // Assert
        verify(cartItemRepository, times(1)).deleteByUserId(testUserId);
        verify(eventPublisher, times(0)).publish(any());
    }

    // Helper methods

    private CartItem createCartItem(Long id, String userId, Long productId, Integer quantity) {
        return CartItem.builder()
                .id(id)
                .cartId("cart-" + userId)
                .userId(userId)
                .productId(productId)
                .quantity(quantity)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
