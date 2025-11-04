package com.ecommerce.shared.events.domain;

import java.util.List;

import com.ecommerce.shared.events.BaseEvent;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Event published when cart validation is requested.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CartValidationRequestedEvent extends BaseEvent {

    @JsonProperty("orderId")
    @NotBlank
    private String orderId;

    @JsonProperty("userId")
    @NotBlank
    private String userId;

    @JsonProperty("items")
    @NotNull
    private List<CartItem> items;

    @JsonProperty("requestingService")
    @NotBlank
    private String requestingService;

    @Builder
    public CartValidationRequestedEvent(String cartId, String orderId, String userId, List<CartItem> items, 
                                      String requestingService, String source, String correlationId) {
        super(cartId, "Cart", source, correlationId);
        this.orderId = orderId;
        this.userId = userId;
        this.items = items;
        this.requestingService = requestingService;
    }

    @Data
    @NoArgsConstructor
    @Builder
    public static class CartItem {
        @JsonProperty("productId")
        @NotBlank
        private String productId;

        @JsonProperty("quantity")
        @NotNull
        private Integer quantity;

        public CartItem(String productId, Integer quantity) {
            this.productId = productId;
            this.quantity = quantity;
        }
    }
}