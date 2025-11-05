package com.ecommerce.cart.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.cart.entity.CartItem;
import com.ecommerce.cart.repository.CartItemRepository;
import com.ecommerce.cart.service.CartCommandService;
import com.ecommerce.shared.testutil.BaseTest;

@Transactional
@DisplayName("Cart Service Integration Tests")
public class CartServiceIntegrationTest extends BaseTest {

    @Autowired
    private CartCommandService cartCommandService;

    @Autowired
    private CartItemRepository cartItemRepository;

    private final String testUserId = "integration-user-123";
    private final String testUserId2 = "integration-user-456";

    @Test
    @DisplayName("Complete cart workflow - add, update, remove items")
    void testCompleteCartWorkflow() {
        // Step 1: Verify empty cart
        List<CartItem> emptyCart = cartItemRepository.findByUserId(testUserId);
        assertNotNull(emptyCart);
        assertEquals(0, emptyCart.size());

        // Step 2: Add first item to cart
        Long cartItemId1 = cartCommandService.addItemToCart(testUserId, 100L, 2);
        assertNotNull(cartItemId1);

        // Step 3: Verify cart has one item
        List<CartItem> cartWithOneItem = cartItemRepository.findByUserId(testUserId);
        assertEquals(1, cartWithOneItem.size());
        assertEquals(100L, cartWithOneItem.get(0).getProductId());
        assertEquals(2, cartWithOneItem.get(0).getQuantity());

        // Step 4: Add second item to cart
        Long cartItemId2 = cartCommandService.addItemToCart(testUserId, 101L, 1);
        assertNotNull(cartItemId2);

        // Step 5: Verify cart has two items
        List<CartItem> cartWithTwoItems = cartItemRepository.findByUserId(testUserId);
        assertEquals(2, cartWithTwoItems.size());
        int totalQuantity = cartWithTwoItems.stream().mapToInt(CartItem::getQuantity).sum();
        assertEquals(3, totalQuantity); // 2 + 1 = 3 total items

        // Step 6: Update first item quantity
        cartCommandService.updateItemQuantity(testUserId, cartItemId1, 5);

        // Step 7: Verify updated quantity
        Optional<CartItem> updatedItem = cartItemRepository.findById(cartItemId1);
        assertTrue(updatedItem.isPresent());
        assertEquals(5, updatedItem.get().getQuantity());

        // Step 8: Remove first item
        cartCommandService.removeItemFromCart(testUserId, cartItemId1);

        // Step 9: Verify only one item remains
        List<CartItem> cartAfterRemoval = cartItemRepository.findByUserId(testUserId);
        assertEquals(1, cartAfterRemoval.size());
        assertEquals(101L, cartAfterRemoval.get(0).getProductId());

        // Step 10: Clear entire cart
        cartCommandService.clearCart(testUserId);

        // Step 11: Verify cart is empty
        List<CartItem> clearedCart = cartItemRepository.findByUserId(testUserId);
        assertEquals(0, clearedCart.size());
    }

    @Test
    @DisplayName("Add duplicate product should increase quantity")
    void testAddDuplicateProduct() {
        // Step 1: Add product for the first time
        cartCommandService.addItemToCart(testUserId, 200L, 2);

        // Verify first addition
        List<CartItem> cart1 = cartItemRepository.findByUserId(testUserId);
        assertEquals(1, cart1.size());
        assertEquals(2, cart1.get(0).getQuantity());

        // Step 2: Add same product again
        cartCommandService.addItemToCart(testUserId, 200L, 3);

        // Step 3: Verify quantity was increased (not duplicated)
        List<CartItem> cart2 = cartItemRepository.findByUserId(testUserId);
        assertEquals(1, cart2.size());
        assertEquals(200L, cart2.get(0).getProductId());
        assertEquals(5, cart2.get(0).getQuantity()); // 2 + 3
    }

    @Test
    @DisplayName("Update quantity to zero should remove item")
    void testUpdateQuantityToZero() {
        // Step 1: Add item
        Long cartItemId = cartCommandService.addItemToCart(testUserId, 300L, 3);

        // Verify item exists
        Optional<CartItem> itemBefore = cartItemRepository.findById(cartItemId);
        assertTrue(itemBefore.isPresent());

        // Step 2: Update quantity to zero
        cartCommandService.updateItemQuantity(testUserId, cartItemId, 0);

        // Step 3: Verify item was removed
        List<CartItem> cart = cartItemRepository.findByUserId(testUserId);
        assertEquals(0, cart.size());

        Optional<CartItem> itemAfter = cartItemRepository.findById(cartItemId);
        assertFalse(itemAfter.isPresent());
    }

    @Test
    @DisplayName("Update quantity to negative should remove item")
    void testUpdateQuantityToNegative() {
        // Step 1: Add item
        Long cartItemId = cartCommandService.addItemToCart(testUserId, 350L, 5);

        // Step 2: Update quantity to negative
        cartCommandService.updateItemQuantity(testUserId, cartItemId, -1);

        // Step 3: Verify item was removed
        List<CartItem> cart = cartItemRepository.findByUserId(testUserId);
        assertEquals(0, cart.size());
    }

    @Test
    @DisplayName("Cart isolation between users")
    void testCartIsolation() {
        // User 1 adds item
        cartCommandService.addItemToCart(testUserId, 400L, 1);

        // User 2 adds different item
        cartCommandService.addItemToCart(testUserId2, 401L, 2);

        // User 1 should only see their item
        List<CartItem> user1Items = cartItemRepository.findByUserId(testUserId);
        assertEquals(1, user1Items.size());
        assertEquals(400L, user1Items.get(0).getProductId());
        assertEquals(1, user1Items.get(0).getQuantity());

        // User 2 should only see their item
        List<CartItem> user2Items = cartItemRepository.findByUserId(testUserId2);
        assertEquals(1, user2Items.size());
        assertEquals(401L, user2Items.get(0).getProductId());
        assertEquals(2, user2Items.get(0).getQuantity());
    }

    @Test
    @DisplayName("Error handling - item not found")
    void testItemNotFoundError() {
        Long nonExistentItemId = 999999L;

        // Attempting to update non-existent item should throw exception
        assertThrows(RuntimeException.class, () -> {
            cartCommandService.updateItemQuantity(testUserId, nonExistentItemId, 5);
        });

        // Attempting to remove non-existent item should throw exception
        assertThrows(RuntimeException.class, () -> {
            cartCommandService.removeItemFromCart(testUserId, nonExistentItemId);
        });
    }

    @Test
    @DisplayName("Clear cart with multiple items")
    void testClearCartWithMultipleItems() {
        // Add multiple items
        cartCommandService.addItemToCart(testUserId, 500L, 2);
        cartCommandService.addItemToCart(testUserId, 501L, 3);
        cartCommandService.addItemToCart(testUserId, 502L, 1);

        // Verify items were added
        List<CartItem> cartBefore = cartItemRepository.findByUserId(testUserId);
        assertEquals(3, cartBefore.size());
        int totalQty = cartBefore.stream().mapToInt(CartItem::getQuantity).sum();
        assertEquals(6, totalQty);

        // Clear cart
        cartCommandService.clearCart(testUserId);

        // Verify cart is empty
        List<CartItem> cartAfter = cartItemRepository.findByUserId(testUserId);
        assertEquals(0, cartAfter.size());
    }

    @Test
    @DisplayName("Clear empty cart should not throw exception")
    void testClearEmptyCart() {
        // Verify cart is empty
        List<CartItem> emptyCart = cartItemRepository.findByUserId(testUserId);
        assertEquals(0, emptyCart.size());

        // Clear empty cart should work without error
        cartCommandService.clearCart(testUserId);

        // Verify still empty
        List<CartItem> stillEmpty = cartItemRepository.findByUserId(testUserId);
        assertEquals(0, stillEmpty.size());
    }

    @Test
    @DisplayName("Updating item quantity multiple times")
    void testUpdateItemQuantityMultipleTimes() {
        // Add item
        Long cartItemId = cartCommandService.addItemToCart(testUserId, 700L, 1);

        // Update multiple times
        cartCommandService.updateItemQuantity(testUserId, cartItemId, 3);
        Optional<CartItem> item1 = cartItemRepository.findById(cartItemId);
        assertTrue(item1.isPresent());
        assertEquals(3, item1.get().getQuantity());

        cartCommandService.updateItemQuantity(testUserId, cartItemId, 7);
        Optional<CartItem> item2 = cartItemRepository.findById(cartItemId);
        assertTrue(item2.isPresent());
        assertEquals(7, item2.get().getQuantity());

        cartCommandService.updateItemQuantity(testUserId, cartItemId, 2);
        Optional<CartItem> item3 = cartItemRepository.findById(cartItemId);
        assertTrue(item3.isPresent());
        assertEquals(2, item3.get().getQuantity());
    }

    @Test
    @DisplayName("Verify cart items persist within transaction")
    void testCartItemPersistence() {
        // Add item
        Long cartItemId = cartCommandService.addItemToCart(testUserId, 800L, 5);

        // Retrieve from repository directly
        Optional<CartItem> item = cartItemRepository.findById(cartItemId);
        assertTrue(item.isPresent());
        assertEquals(testUserId, item.get().getUserId());
        assertEquals(800L, item.get().getProductId());
        assertEquals(5, item.get().getQuantity());
        assertNotNull(item.get().getCartId());
        assertNotNull(item.get().getCreatedAt());
        assertNotNull(item.get().getUpdatedAt());
    }
}
