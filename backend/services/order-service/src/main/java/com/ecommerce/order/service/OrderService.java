package com.ecommerce.order.service;

import java.util.List;

import com.ecommerce.order.dto.CreateOrderRequest;
import com.ecommerce.order.dto.OrderDto;

/**
 * Interface for order service operations
 */
public interface OrderService {
    
    /**
     * Get all orders for a user
     */
    List<OrderDto> getUserOrders(String userId);
    
    /**
     * Get a specific order for a user
     */
    OrderDto getOrder(Long id, String userId);
    
    /**
     * Create a new order
     */
    OrderDto createOrder(CreateOrderRequest request, String userId);
    
    /**
     * Cancel an order
     */
    OrderDto cancelOrder(Long id, String userId);
    
    /**
     * Get all orders (admin only)
     */
    List<OrderDto> getAllOrders();
}