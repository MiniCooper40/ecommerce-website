package com.ecommerce.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.ecommerce.order.entity.AddressType;

/**
 * DTO for creating a new address
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateAddressRequest {
    
    @NotNull(message = "Address type is required")
    private AddressType type;
    
    @NotBlank(message = "Street is required")
    private String street;
    
    @NotBlank(message = "City is required")
    private String city;
    
    @NotBlank(message = "State is required")
    private String state;
    
    @NotBlank(message = "ZIP code is required")
    private String zipCode;
    
    @NotBlank(message = "Country is required")
    private String country;
}
