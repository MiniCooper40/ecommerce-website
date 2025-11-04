package com.ecommerce.shared.events.domain;

import com.ecommerce.shared.events.BaseEvent;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Event published when a cart item is updated (quantity changed).
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CartItemUpdatedEvent extends BaseEvent {

    @JsonProperty("cartItemId")
    @NotNull
    private Long cartItemId;

    @JsonProperty("cartId")
    @NotNull
    private String cartId;

    @JsonProperty("userId")
    @NotNull
    private String userId;

    @JsonProperty("productId")
    @NotNull
    private Long productId;

    @JsonProperty("quantity")
    @NotNull
    private Integer quantity;

    @Builder
    public CartItemUpdatedEvent(Long cartItemId, String cartId, String userId, 
                               Long productId, Integer quantity, 
                               String source, String correlationId) {
        super(cartItemId.toString(), "Cart", source, correlationId);
        this.cartItemId = cartItemId;
        this.cartId = cartId;
        this.userId = userId;
        this.productId = productId;
        this.quantity = quantity;
    }
}
