package com.ecommerce.order.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.order.dto.CreateOrderRequest;
import com.ecommerce.order.dto.OrderDto;
import com.ecommerce.order.entity.Address;
import com.ecommerce.order.entity.AddressType;
import com.ecommerce.order.entity.Order;
import com.ecommerce.order.entity.OrderItem;
import com.ecommerce.order.entity.OrderStatus;
import com.ecommerce.order.mapper.OrderMapper;
import com.ecommerce.order.repository.OrderRepository;

/**
 * Service implementation class for order operations
 */
@Service
@Transactional
public class OrderServiceImpl implements OrderService {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private AddressService addressService;
    
    @Autowired
    private OrderMapper orderMapper;
    
    /**
     * Get all orders for a user
     */
    @Override
    @Transactional(readOnly = true)
    public List<OrderDto> getUserOrders(String userId) {
        List<Order> orders = orderRepository.findByUserIdWithItems(userId);
        return orderMapper.toDtoList(orders);
    }
    
    /**
     * Get a specific order for a user
     */
    @Override
    @Transactional(readOnly = true)
    public OrderDto getOrder(Long id, String userId) {
        Order order = orderRepository.findByIdAndUserIdWithItems(id, userId)
                .orElseThrow(() -> new RuntimeException("Order not found or not accessible"));
        return orderMapper.toDto(order);
    }
    
    /**
     * Create a new order
     */
    @Override
    public OrderDto createOrder(CreateOrderRequest request, String userId) {
        // Create addresses first
        Address shippingAddress = addressService.convertToEntity(request.getShippingAddress());
        shippingAddress.setType(AddressType.SHIPPING);
        
        Address billingAddress = addressService.convertToEntity(request.getBillingAddress());
        billingAddress.setType(AddressType.BILLING);
        
        // Create the order
        Order order = new Order();
        order.setUserId(userId);
        order.setStatus(OrderStatus.PENDING);
        order.setShippingAddress(shippingAddress);
        order.setBillingAddress(billingAddress);
        
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
        return orderMapper.toDto(savedOrder);
    }
    
    /**
     * Cancel an order
     */
    @Override
    public OrderDto cancelOrder(Long id, String userId) {
        Order order = orderRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Order not found or not accessible"));
        
        if (order.getStatus() == OrderStatus.DELIVERED || order.getStatus() == OrderStatus.CANCELLED) {
            throw new RuntimeException("Cannot cancel order in status: " + order.getStatus());
        }
        
        order.setStatus(OrderStatus.CANCELLED);
        Order savedOrder = orderRepository.save(order);
        return orderMapper.toDto(savedOrder);
    }
    
    /**
     * Get all orders (admin only)
     */
    @Override
    @Transactional(readOnly = true)
    public List<OrderDto> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        return orderMapper.toDtoList(orders);
    }
}
