package com.ecommerce.order.repository;

import java.util.List;
import java.util.Optional;

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
    
    /**
     * Find all addresses for a specific user
     */
    List<Address> findByUserIdOrderByCreatedAtDesc(String userId);
    
    /**
     * Find an address by ID and user ID (for security - users can only access their own addresses)
     */
    Optional<Address> findByIdAndUserId(Long id, String userId);
    
    /**
     * Find addresses by user ID and type
     */
    List<Address> findByUserIdAndTypeOrderByCreatedAtDesc(String userId, AddressType type);
    
    /**
     * Count addresses by user ID
     */
    long countByUserId(String userId);
}
