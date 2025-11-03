package com.ecommerce.cart.controller;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import com.ecommerce.cart.dto.CartItemDto;
import com.ecommerce.cart.dto.CartSummaryDto;
import com.ecommerce.cart.service.CartService;
import com.ecommerce.shared.testutil.WithMockUserPrincipal;

@SpringBootTest
@AutoConfigureMockMvc
public class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CartService cartService;

    @Test
    void shouldReturnUnauthorizedForNoAuth() throws Exception {
        mockMvc.perform(get("/cart"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUserPrincipal(userId = "test-user", roles = {"USER"})
    void shouldReturnOkForAuthenticatedUser() throws Exception {
        // Mock the service to return an empty cart
        CartSummaryDto mockCart = new CartSummaryDto();
        mockCart.setItems(List.of()); // This will automatically calculate totals
        
        when(cartService.getCart(anyString())).thenReturn(mockCart);
        
        mockMvc.perform(get("/cart"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItems").value(0))
                .andExpect(jsonPath("$.total").value(0));
    }

    @Test
    @WithMockUserPrincipal(userId = "test-user", roles = {"USER"})
    void shouldReturnCartWithItems() throws Exception {
        // Mock the service to return a cart with items
        CartItemDto mockItem = CartItemDto.builder()
                .id(1L)
                .productId(100L)
                .productName("Test Product")
                .productPrice(BigDecimal.valueOf(25.99))
                .quantity(2)
                .build();
        
        CartSummaryDto mockCart = new CartSummaryDto();
        mockCart.setItems(List.of(mockItem)); // This will automatically calculate totals
        
        when(cartService.getCart("test-user")).thenReturn(mockCart);
        
        mockMvc.perform(get("/cart"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItems").value(2))
                .andExpect(jsonPath("$.items[0].productName").value("Test Product"))
                .andExpect(jsonPath("$.items[0].quantity").value(2))
                .andExpect(jsonPath("$.subtotal").value(51.98));
    }
    
}
