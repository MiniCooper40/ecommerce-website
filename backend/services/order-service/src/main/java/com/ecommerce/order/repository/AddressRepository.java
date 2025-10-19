package com.ecommerce.order.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ecommerce.order.entity.Address;
import com.ecommerce.order.entity.AddressType;

/**
 * Repository interface for Address entity
 */
@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    
    /**
     * Find addresses by type
     */
    List<Address> findByType(AddressType type);
}
