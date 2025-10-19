package com.ecommerce.order.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.order.dto.AddressDto;
import com.ecommerce.order.dto.CreateAddressRequest;
import com.ecommerce.order.entity.Address;
import com.ecommerce.order.entity.AddressType;
import com.ecommerce.order.mapper.AddressMapper;
import com.ecommerce.order.repository.AddressRepository;

/**
 * Service implementation class for address operations
 */
@Service
@Transactional
public class AddressServiceImpl implements AddressService {
    
    @Autowired
    private AddressRepository addressRepository;
    
    @Autowired
    private AddressMapper addressMapper;
    
    /**
     * Create a new address
     */
    @Override
    public AddressDto createAddress(CreateAddressRequest request) {
        Address address = addressMapper.toEntity(request);
        Address savedAddress = addressRepository.save(address);
        return addressMapper.toDto(savedAddress);
    }
    
    /**
     * Get address by ID
     */
    @Override
    @Transactional(readOnly = true)
    public AddressDto getAddress(Long id) {
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Address not found with id: " + id));
        return addressMapper.toDto(address);
    }
    
    /**
     * Get all addresses by type
     */
    @Override
    @Transactional(readOnly = true)
    public List<AddressDto> getAddressesByType(AddressType type) {
        List<Address> addresses = addressRepository.findByType(type);
        return addresses.stream()
                .map(addressMapper::toDto)
                .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * Convert CreateAddressRequest to Address entity
     */
    @Override
    public Address convertToEntity(CreateAddressRequest request) {
        return addressMapper.toEntity(request);
    }
    
    /**
     * Create address from individual components
     */
    @Override
    public Address createAddressEntity(AddressType type, String street, String city, String state, String zipCode, String country) {
        Address address = new Address();
        address.setType(type);
        address.setStreet(street);
        address.setCity(city);
        address.setState(state);
        address.setZipCode(zipCode);
        address.setCountry(country);
        return address;
    }
    
    /**
     * Parse full address string into components and create entity
     * This is useful for legacy address strings
     */
    @Override
    public Address parseAndCreateAddress(AddressType type, String fullAddress) {
        // Handle null and empty cases
        if (fullAddress == null || fullAddress.trim().isEmpty()) {
            throw new IllegalArgumentException("Full address cannot be null or empty");
        }
        
        // Simple parsing implementation - in a real application, this would be more sophisticated
        // Expected format: "street, city, state zipcode, country"
        String[] parts = fullAddress.split(",");
        if (parts.length < 3) {
            throw new IllegalArgumentException("Failed to parse address: Invalid format. Expected: 'street, city, state zipcode, country'");
        }
        
        String street = parts[0].trim();
        String city = parts[1].trim();
        String stateAndZip = parts[2].trim();
        String country = parts.length > 3 ? parts[3].trim() : "USA";
        
        // Parse state and zip from "state zipcode"
        String[] stateZipParts = stateAndZip.split("\\s+");
        String state = stateZipParts[0];
        String zipCode = stateZipParts.length > 1 ? stateZipParts[1] : "";
        
        return createAddressEntity(type, street, city, state, zipCode, country);
    }
}
