package com.ecommerce.order.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.order.dto.CreateOrderRequest;
import com.ecommerce.order.dto.OrderDto;
import com.ecommerce.order.dto.OrderItemDto;
import com.ecommerce.order.entity.Order;
import com.ecommerce.order.entity.OrderItem;
import com.ecommerce.order.entity.OrderStatus;
import com.ecommerce.order.repository.OrderRepository;

/**
 * Service class for order operations
 */
@Service
@Transactional
public class OrderService {
    
    @Autowired
    private OrderRepository orderRepository;
    
    /**
     * Get all orders for a user
     */
    @Transactional(readOnly = true)
    public List<OrderDto> getUserOrders(String userId) {
        List<Order> orders = orderRepository.findByUserIdWithItems(userId);
        return orders.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Get a specific order for a user
     */
    @Transactional(readOnly = true)
    public OrderDto getOrder(Long id, String userId) {
        Order order = orderRepository.findByIdAndUserIdWithItems(id, userId)
                .orElseThrow(() -> new RuntimeException("Order not found or not accessible"));
        return convertToDto(order);
    }
    
    /**
     * Create a new order
     */
    public OrderDto createOrder(CreateOrderRequest request, String userId) {
        // Create the order
        Order order = new Order();
        order.setUserId(userId);
        order.setStatus(OrderStatus.PENDING);
        order.setShippingAddress(request.getShippingAddress());
        order.setBillingAddress(request.getBillingAddress());
        
        // Create order items (in a real app, you'd fetch product details from catalog service)
        List<OrderItem> orderItems = request.getItems().stream()
                .map(itemRequest -> {
                    // In reality, you'd call the catalog service to get product details and price
                    OrderItem item = new OrderItem();
                    item.setOrder(order);
                    item.setProductId(itemRequest.getProductId());
                    item.setProductName("Product " + itemRequest.getProductId()); // Mock product name
                    item.setQuantity(itemRequest.getQuantity());
                    item.setPrice(new BigDecimal("10.00")); // Mock price
                    item.setSubtotal(item.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity())));
                    return item;
                })
                .collect(Collectors.toList());
        
        order.setItems(orderItems);
        
        // Calculate total amount
        BigDecimal totalAmount = orderItems.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalAmount(totalAmount);
        
        // Save the order
        Order savedOrder = orderRepository.save(order);
        return convertToDto(savedOrder);
    }
    
    /**
     * Cancel an order
     */
    public OrderDto cancelOrder(Long id, String userId) {
        Order order = orderRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Order not found or not accessible"));
        
        if (order.getStatus() == OrderStatus.DELIVERED || order.getStatus() == OrderStatus.CANCELLED) {
            throw new RuntimeException("Cannot cancel order in status: " + order.getStatus());
        }
        
        order.setStatus(OrderStatus.CANCELLED);
        Order savedOrder = orderRepository.save(order);
        return convertToDto(savedOrder);
    }
    
    /**
     * Get all orders (admin only)
     */
    @Transactional(readOnly = true)
    public List<OrderDto> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        return orders.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Convert Order entity to OrderDto
     */
    private OrderDto convertToDto(Order order) {
        List<OrderItemDto> itemDtos = order.getItems() != null 
                ? order.getItems().stream()
                    .map(this::convertItemToDto)
                    .collect(Collectors.toList())
                : List.of();
        
        return new OrderDto(
                order.getId(),
                order.getUserId(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                order.getShippingAddress(),
                order.getBillingAddress(),
                itemDtos
        );
    }
    
    /**
     * Convert OrderItem entity to OrderItemDto
     */
    private OrderItemDto convertItemToDto(OrderItem item) {
        return new OrderItemDto(
                item.getId(),
                item.getProductId(),
                item.getProductName(),
                item.getQuantity(),
                item.getPrice(),
                item.getSubtotal()
        );
    }
}