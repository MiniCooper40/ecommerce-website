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
 * Event published when cart validation is completed.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CartValidationCompletedEvent extends BaseEvent {

    @JsonProperty("userId")
    @NotBlank
    private String userId;

    @JsonProperty("isValid")
    @NotNull
    private Boolean isValid;

    @JsonProperty("validationErrors")
    private List<String> validationErrors;

    @JsonProperty("requestingService")
    @NotBlank
    private String requestingService;

    @Builder
    public CartValidationCompletedEvent(String cartId, String userId, Boolean isValid, 
                                      List<String> validationErrors, String requestingService, 
                                      String source, String correlationId) {
        super(cartId, "Cart", source, correlationId);
        this.userId = userId;
        this.isValid = isValid;
        this.validationErrors = validationErrors;
        this.requestingService = requestingService;
    }
}