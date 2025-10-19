package com.ecommerce.order.service;

import java.util.List;

import com.ecommerce.order.dto.AddressDto;
import com.ecommerce.order.dto.CreateAddressRequest;
import com.ecommerce.order.entity.Address;
import com.ecommerce.order.entity.AddressType;

/**
 * Interface for address service operations
 */
public interface AddressService {
    
    /**
     * Create a new address
     */
    AddressDto createAddress(CreateAddressRequest request);
    
    /**
     * Get address by ID
     */
    AddressDto getAddress(Long id);
    
    /**
     * Get all addresses by type
     */
    List<AddressDto> getAddressesByType(AddressType type);
    
    /**
     * Convert CreateAddressRequest to Address entity
     */
    Address convertToEntity(CreateAddressRequest request);
    
    /**
     * Create address from individual components
     */
    Address createAddressEntity(AddressType type, String street, String city, String state, String zipCode, String country);
    
    /**
     * Parse full address string into components and create entity
     * This is useful for legacy address strings
     */
    Address parseAndCreateAddress(AddressType type, String fullAddress);
}
