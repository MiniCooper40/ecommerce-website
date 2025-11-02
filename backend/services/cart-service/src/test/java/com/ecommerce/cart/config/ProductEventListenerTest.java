package com.ecommerce.cart.config;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ecommerce.cart.service.CartService;
import com.ecommerce.shared.events.domain.ProductUpdatedEvent;

/**
 * Unit tests for ProductEventListener
 */
@ExtendWith(MockitoExtension.class)
class ProductEventListenerTest {

    @Mock
    private CartService cartService;

    @InjectMocks
    private ProductEventListener productEventListener;

    @Test
    void handleProductUpdated_ShouldUpdateCartItems() {
        // Given
        ProductUpdatedEvent event = ProductUpdatedEvent.builder()
                .productId("123")
                .name("Updated Product Name")
                .price(new BigDecimal("99.99"))
                .imageUrl("https://example.com/updated-image.jpg")
                .source("catalog-service")
                .correlationId("test-correlation-id")
                .build();

        // When
        productEventListener.handleProductUpdated(event, "product-events", 0, 100L);

        // Then
        verify(cartService).updateCartItemsForProduct(
                eq(123L),
                eq("Updated Product Name"),
                eq(new BigDecimal("99.99")),
                eq("https://example.com/updated-image.jpg")
        );
    }
}