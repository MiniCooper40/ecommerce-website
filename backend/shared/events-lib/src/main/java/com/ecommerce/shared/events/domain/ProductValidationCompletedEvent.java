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
 * Event published when product validation is completed.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ProductValidationCompletedEvent extends BaseEvent {

    @JsonProperty("validProducts")
    @NotNull
    private List<String> validProducts;

    @JsonProperty("invalidProducts")
    private List<String> invalidProducts;

    @JsonProperty("unavailableProducts")
    private List<ProductAvailability> unavailableProducts;

    @JsonProperty("isValid")
    @NotNull
    private Boolean isValid;

    @JsonProperty("requestingService")
    @NotBlank
    private String requestingService;

    @Builder
    public ProductValidationCompletedEvent(String requestId, List<String> validProducts, 
                                         List<String> invalidProducts, List<ProductAvailability> unavailableProducts,
                                         Boolean isValid, String requestingService, String source, String correlationId) {
        super(requestId, "Product", source, correlationId);
        this.validProducts = validProducts;
        this.invalidProducts = invalidProducts;
        this.unavailableProducts = unavailableProducts;
        this.isValid = isValid;
        this.requestingService = requestingService;
    }

    @Data
    @NoArgsConstructor
    @Builder
    public static class ProductAvailability {
        @JsonProperty("productId")
        @NotBlank
        private String productId;

        @JsonProperty("availableQuantity")
        @NotNull
        private Integer availableQuantity;

        @JsonProperty("requestedQuantity")
        @NotNull
        private Integer requestedQuantity;

        public ProductAvailability(String productId, Integer availableQuantity, Integer requestedQuantity) {
            this.productId = productId;
            this.availableQuantity = availableQuantity;
            this.requestedQuantity = requestedQuantity;
        }
    }
}