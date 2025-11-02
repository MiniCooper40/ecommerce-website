package com.ecommerce.shared.events.domain;

import com.ecommerce.shared.events.BaseEvent;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Event published when a new order is created.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class OrderCreatedEvent extends BaseEvent {

    @JsonProperty("userId")
    @NotBlank
    private String userId;

    @JsonProperty("totalAmount")
    @NotNull
    @Positive
    private BigDecimal totalAmount;

    @JsonProperty("currency")
    @NotBlank
    private String currency;

    @JsonProperty("items")
    @NotNull
    private List<OrderItem> items;

    @JsonProperty("shippingAddress")
    private String shippingAddress;

    @JsonProperty("status")
    @NotBlank
    private String status = "CREATED";

    @Builder
    public OrderCreatedEvent(String orderId, String userId, BigDecimal totalAmount, 
                           String currency, List<OrderItem> items, String shippingAddress,
                           String source, String correlationId) {
        super(orderId, "Order", source, correlationId);
        this.userId = userId;
        this.totalAmount = totalAmount;
        this.currency = currency;
        this.items = items;
        this.shippingAddress = shippingAddress;
        this.status = "CREATED";
    }

    @Data
    @NoArgsConstructor
    @Builder
    public static class OrderItem {
        @JsonProperty("productId")
        @NotBlank
        private String productId;

        @JsonProperty("quantity")
        @Positive
        private Integer quantity;

        @JsonProperty("unitPrice")
        @NotNull
        @Positive
        private BigDecimal unitPrice;

        @JsonProperty("productName")
        private String productName;

        public OrderItem(String productId, Integer quantity, BigDecimal unitPrice, String productName) {
            this.productId = productId;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.productName = productName;
        }
    }
}