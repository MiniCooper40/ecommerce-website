package com.ecommerce.shared.events.domain;

import com.ecommerce.shared.events.BaseEvent;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Event published when a product is updated.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ProductUpdatedEvent extends BaseEvent {

    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    @JsonProperty("price")
    @Positive
    private BigDecimal price;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("stockQuantity")
    private Integer stockQuantity;

    @JsonProperty("category")
    private String category;

    @JsonProperty("imageUrl")
    private String imageUrl;

    @JsonProperty("active")
    @NotNull
    private Boolean active;

    @Builder
    public ProductUpdatedEvent(String productId, String name, String description, 
                             BigDecimal price, String currency, Integer stockQuantity, 
                             String category, String imageUrl, Boolean active, 
                             String source, String correlationId) {
        super(productId, "Product", source, correlationId);
        this.name = name;
        this.description = description;
        this.price = price;
        this.currency = currency;
        this.stockQuantity = stockQuantity;
        this.category = category;
        this.imageUrl = imageUrl;
        this.active = active;
    }
}