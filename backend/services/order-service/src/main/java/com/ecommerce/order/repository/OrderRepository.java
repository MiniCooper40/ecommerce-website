package com.ecommerce.order.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ecommerce.order.entity.Order;
import com.ecommerce.order.entity.OrderStatus;

/**
 * Repository interface for Order entity
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    /**
     * Find all orders for a specific user
     */
    List<Order> findByUserIdOrderByCreatedAtDesc(String userId);
    
    /**
     * Find an order by ID and user ID (for security - users can only access their own orders)
     */
    Optional<Order> findByIdAndUserId(Long id, String userId);
    
    /**
     * Find orders by user ID and status
     */
    List<Order> findByUserIdAndStatusOrderByCreatedAtDesc(String userId, String status);
    
    /**
     * Count orders by user ID
     */
    long countByUserId(String userId);
    
    /**
     * Find orders with items eagerly loaded
     */
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items WHERE o.userId = :userId ORDER BY o.createdAt DESC")
    List<Order> findByUserIdWithItems(@Param("userId") String userId);
    
    /**
     * Find a specific order with items eagerly loaded
     */
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items WHERE o.id = :id AND o.userId = :userId")
    Optional<Order> findByIdAndUserIdWithItems(@Param("id") Long id, @Param("userId") String userId);
    
    /**
     * Find the most recent order by user ID and status
     */
    Optional<Order> findFirstByUserIdAndStatusOrderByCreatedAtDesc(String userId, OrderStatus status);
}
