package com.ecommerce.shared.events.domain;

import com.ecommerce.shared.events.BaseEvent;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Event published when product validation is requested.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ProductValidationRequestedEvent extends BaseEvent {

    @JsonProperty("productIds")
    @NotNull
    private List<String> productIds;

    @JsonProperty("requestingService")
    @NotBlank
    private String requestingService;

    @JsonProperty("requiredQuantities")
    private List<ProductQuantity> requiredQuantities;

    @Builder
    public ProductValidationRequestedEvent(String requestId, List<String> productIds, 
                                         List<ProductQuantity> requiredQuantities,
                                         String requestingService, String source, String correlationId) {
        super(requestId, "Product", source, correlationId);
        this.productIds = productIds;
        this.requiredQuantities = requiredQuantities;
        this.requestingService = requestingService;
    }

    @Data
    @NoArgsConstructor
    @Builder
    public static class ProductQuantity {
        @JsonProperty("productId")
        @NotBlank
        private String productId;

        @JsonProperty("requiredQuantity")
        @NotNull
        private Integer requiredQuantity;

        public ProductQuantity(String productId, Integer requiredQuantity) {
            this.productId = productId;
            this.requiredQuantity = requiredQuantity;
        }
    }
}