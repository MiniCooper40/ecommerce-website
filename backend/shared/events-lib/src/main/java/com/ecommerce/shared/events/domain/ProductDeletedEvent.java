package com.ecommerce.shared.events.domain;

import com.ecommerce.shared.events.BaseEvent;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Event published when a product is deleted (soft deleted by setting isActive to false).
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ProductDeletedEvent extends BaseEvent {

    @JsonProperty("name")
    private String name;

    @JsonProperty("category")
    private String category;

    @Builder
    public ProductDeletedEvent(String productId, String name, String category, 
                              String source, String correlationId) {
        super(productId, "Product", source, correlationId);
        this.name = name;
        this.category = category;
    }
}
