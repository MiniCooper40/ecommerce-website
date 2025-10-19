package com.ecommerce.order.mapper;

import org.springframework.stereotype.Component;

import com.ecommerce.order.dto.AddressDto;
import com.ecommerce.order.dto.CreateAddressRequest;
import com.ecommerce.order.entity.Address;

/**
 * Mapper component for converting between Address entities and DTOs
 * Following the common Spring pattern of having dedicated mapper beans
 */
@Component
public class AddressMapper {

    /**
     * Convert Address entity to AddressDto
     * @param address The address entity to convert
     * @return AddressDto or null if input is null
     */
    public AddressDto toDto(Address address) {
        if (address == null) {
            return null;
        }
        
        return new AddressDto(
                address.getId(),
                address.getType(),
                address.getStreet(),
                address.getCity(),
                address.getState(),
                address.getZipCode(),
                address.getCountry(),
                address.getCreatedAt(),
                address.getUpdatedAt()
        );
    }

    /**
     * Convert CreateAddressRequest to Address entity
     * @param request The address request to convert
     * @return Address entity
     */
    public Address toEntity(CreateAddressRequest request) {
        if (request == null) {
            return null;
        }
        
        Address address = new Address();
        address.setType(request.getType());
        address.setStreet(request.getStreet());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setZipCode(request.getZipCode());
        address.setCountry(request.getCountry());
        
        return address;
    }
}
