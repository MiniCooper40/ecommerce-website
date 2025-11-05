package com.ecommerce.cart.controller;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.ecommerce.cart.dto.AddCartItemRequest;
import com.ecommerce.cart.dto.CartItemDto;
import com.ecommerce.cart.dto.CartSummaryDto;
import com.ecommerce.cart.service.CartCommandService;
import com.ecommerce.cart.service.CartQueryService;
import com.ecommerce.shared.testutil.BaseTest;
import com.ecommerce.shared.testutil.WithMockUserPrincipal;
import com.fasterxml.jackson.databind.ObjectMapper;

@AutoConfigureMockMvc
@DisplayName("Cart Controller Tests")
public class CartControllerTest extends BaseTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CartCommandService cartCommandService;

    @MockBean
    private CartQueryService cartQueryService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUserPrincipal(userId = "user123")
    @DisplayName("GET /cart - should return user's cart")
    public void testGetCart_Success() throws Exception {
        // Arrange
        CartSummaryDto cartSummary = createMockCartSummary();
        when(cartQueryService.getCart("user123")).thenReturn(cartSummary);

        // Act & Assert
        mockMvc.perform(get("/cart"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.totalItems").value(3))
                .andExpect(jsonPath("$.subtotal").value(89.97)); // 29.99 * 1 + 29.99 * 2

        verify(cartQueryService, times(1)).getCart("user123");
    }

    @Test
    @WithMockUserPrincipal(userId = "user123")
    @DisplayName("GET /cart - should return empty cart")
    public void testGetCart_EmptyCart() throws Exception {
        // Arrange
        CartSummaryDto emptyCart = new CartSummaryDto(Collections.emptyList());
        when(cartQueryService.getCart("user123")).thenReturn(emptyCart);

        // Act & Assert
        mockMvc.perform(get("/cart"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isEmpty())
                .andExpect(jsonPath("$.totalItems").value(0))
                .andExpect(jsonPath("$.subtotal").value(0));
    }

    @Test
    @WithMockUserPrincipal(userId = "user123")
    @DisplayName("GET /cart/count - should return cart item count")
    public void testGetCartItemCount_Success() throws Exception {
        // Arrange
        when(cartQueryService.getCartItemCount("user123")).thenReturn(5);

        // Act & Assert
        mockMvc.perform(get("/cart/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(5));

        verify(cartQueryService, times(1)).getCartItemCount("user123");
    }

    @Test
    @WithMockUserPrincipal(userId = "user123")
    @DisplayName("POST /cart/items - should add item to cart")
    public void testAddItemToCart_Success() throws Exception {
        // Arrange
        AddCartItemRequest request = AddCartItemRequest.builder()
                .productId(1L)
                .quantity(2)
                .build();

        when(cartCommandService.addItemToCart(eq("user123"), eq(1L), eq(2))).thenReturn(10L);

        // Act & Assert
        mockMvc.perform(post("/cart/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(10));

        verify(cartCommandService, times(1)).addItemToCart("user123", 1L, 2);
    }

    @Test
    @WithMockUserPrincipal(userId = "user123")
    @DisplayName("POST /cart/items - should fail with invalid request")
    public void testAddItemToCart_InvalidRequest() throws Exception {
        // Arrange - missing productId and invalid quantity
        AddCartItemRequest request = AddCartItemRequest.builder()
                .quantity(0) // Invalid - must be at least 1
                .build();

        // Act & Assert
        mockMvc.perform(post("/cart/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUserPrincipal(userId = "user123")
    @DisplayName("POST /cart/items - should fail with null quantity")
    public void testAddItemToCart_NullQuantity() throws Exception {
        // Arrange
        AddCartItemRequest request = AddCartItemRequest.builder()
                .productId(1L)
                .quantity(null)
                .build();

        // Act & Assert
        mockMvc.perform(post("/cart/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUserPrincipal(userId = "user123")
    @DisplayName("PUT /cart/items/{itemId}/quantity - should update item quantity")
    public void testUpdateItemQuantity_Success() throws Exception {
        // Arrange
        doNothing().when(cartCommandService).updateItemQuantity(anyString(), anyLong(), anyInt());

        // Act & Assert
        mockMvc.perform(put("/cart/items/10/quantity")
                .param("quantity", "5"))
                .andExpect(status().isNoContent());

        verify(cartCommandService, times(1)).updateItemQuantity("user123", 10L, 5);
    }

    @Test
    @WithMockUserPrincipal(userId = "user123")
    @DisplayName("DELETE /cart/items/{itemId} - should remove item from cart")
    public void testRemoveItemFromCart_Success() throws Exception {
        // Arrange
        doNothing().when(cartCommandService).removeItemFromCart(anyString(), anyLong());

        // Act & Assert
        mockMvc.perform(delete("/cart/items/10"))
                .andExpect(status().isNoContent());

        verify(cartCommandService, times(1)).removeItemFromCart("user123", 10L);
    }

    @Test
    @WithMockUserPrincipal(userId = "user123")
    @DisplayName("DELETE /cart - should clear entire cart")
    public void testClearCart_Success() throws Exception {
        // Arrange
        doNothing().when(cartCommandService).clearCart(anyString());

        // Act & Assert
        mockMvc.perform(delete("/cart"))
                .andExpect(status().isNoContent());

        verify(cartCommandService, times(1)).clearCart("user123");
    }

    @Test
    @DisplayName("GET /cart - should return 401 without authentication")
    public void testGetCart_Unauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/cart"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /cart/items - should return 401 without authentication")
    public void testAddItemToCart_Unauthorized() throws Exception {
        // Arrange
        AddCartItemRequest request = AddCartItemRequest.builder()
                .productId(1L)
                .quantity(2)
                .build();

        // Act & Assert
        mockMvc.perform(post("/cart/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // Helper methods

    private CartSummaryDto createMockCartSummary() {
        CartItemDto item1 = CartItemDto.builder()
                .id(1L)
                .productId(100L)
                .productName("Test Product 1")
                .productPrice(new BigDecimal("29.99"))
                .productImageUrl("http://example.com/image1.jpg")
                .quantity(1)
                .available(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        CartItemDto item2 = CartItemDto.builder()
                .id(2L)
                .productId(101L)
                .productName("Test Product 2")
                .productPrice(new BigDecimal("29.99"))
                .productImageUrl("http://example.com/image2.jpg")
                .quantity(2)
                .available(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return new CartSummaryDto(Arrays.asList(item1, item2));
    }
}
