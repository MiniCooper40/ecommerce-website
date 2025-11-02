package com.ecommerce.cart.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ecommerce.cart.entity.CartItem;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    
    List<CartItem> findByUserId(String userId);
    
    Optional<CartItem> findByUserIdAndProductId(String userId, Long productId);
    
    Optional<CartItem> findByUserIdAndId(String userId, Long id);
    
    void deleteByUserId(String userId);
    
    void deleteByUserIdAndId(String userId, Long id);
    
    @Query("SELECT COUNT(c) FROM CartItem c WHERE c.userId = :userId")
    Long countByUserId(@Param("userId") String userId);
    
    @Query("SELECT SUM(c.quantity) FROM CartItem c WHERE c.userId = :userId")
    Integer sumQuantityByUserId(@Param("userId") String userId);
    
    boolean existsByUserIdAndProductId(String userId, Long productId);
    
    List<CartItem> findByProductId(Long productId);
}