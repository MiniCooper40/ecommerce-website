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
     * Create a new address for a user
     */
    AddressDto createAddress(CreateAddressRequest request, String userId);
    
    /**
     * Get address by ID (for any user - admin only)
     */
    AddressDto getAddress(Long id);
    
    /**
     * Get address by ID for a specific user (security check)
     */
    AddressDto getUserAddress(Long id, String userId);
    
    /**
     * Get all addresses for a user
     */
    List<AddressDto> getUserAddresses(String userId);
    
    /**
     * Get all addresses by type for a user
     */
    List<AddressDto> getUserAddressesByType(String userId, AddressType type);
    
    /**
     * Get all addresses by type (admin only)
     */
    List<AddressDto> getAddressesByType(AddressType type);
    
    /**
     * Get all addresses (admin only)
     */
    List<AddressDto> getAllAddresses();
    
    /**
     * Convert CreateAddressRequest to Address entity
     */
    Address convertToEntity(CreateAddressRequest request);
    
    /**
     * Create address from individual components
     */
    Address createAddressEntity(AddressType type, String street, String city, String state, String zipCode, String country);
    
    /**
     * Create address from individual components with user ID
     */
    Address createAddressEntity(String userId, AddressType type, String street, String city, String state, String zipCode, String country);
    
    /**
     * Parse full address string into components and create entity
     * This is useful for legacy address strings
     */
    Address parseAndCreateAddress(AddressType type, String fullAddress);
}
