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
import com.ecommerce.shared.events.EventPublisher;
import com.ecommerce.shared.events.domain.CartValidationRequestedEvent;
import com.ecommerce.shared.events.domain.ProductValidationRequestedEvent;

import lombok.extern.slf4j.Slf4j;

/**
 * Service implementation class for order operations
 */
@Slf4j
@Service
@Transactional
public class OrderServiceImpl implements OrderService {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private AddressService addressService;
    
    @Autowired
    private OrderMapper orderMapper;
    
    @Autowired
    private EventPublisher eventPublisher;
    
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
        shippingAddress.setUserId(userId);
        shippingAddress.setType(AddressType.SHIPPING);
        
        Address billingAddress = addressService.convertToEntity(request.getBillingAddress());
        billingAddress.setUserId(userId);
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
        
        // Publish validation request events
        publishValidationRequests(savedOrder, request);
        
        return orderMapper.toDto(savedOrder);
    }
    
    /**
     * Publish validation request events for cart and stock validation
     */
    private void publishValidationRequests(Order order, CreateOrderRequest request) {
        try {
            // Build cart items for validation
            List<CartValidationRequestedEvent.CartItem> cartItems = request.getItems().stream()
                    .map(item -> CartValidationRequestedEvent.CartItem.builder()
                            .productId(item.getProductId().toString())
                            .quantity(item.getQuantity())
                            .build())
                    .collect(Collectors.toList());
            
            // Publish cart validation request
            CartValidationRequestedEvent cartValidationEvent = CartValidationRequestedEvent.builder()
                    .cartId("cart-" + order.getUserId()) // In real app, would have actual cart ID
                    .orderId(order.getId().toString())
                    .userId(order.getUserId())
                    .items(cartItems)
                    .requestingService("order-service")
                    .source("order-service")
                    .correlationId(order.getId().toString())
                    .build();
            
            eventPublisher.publish(cartValidationEvent);
            log.info("Published CartValidationRequestedEvent for order: {}", order.getId());
            
            // Build product quantities for validation
            List<ProductValidationRequestedEvent.ProductQuantity> productQuantities = 
                    request.getItems().stream()
                            .map(item -> ProductValidationRequestedEvent.ProductQuantity.builder()
                                    .productId(item.getProductId().toString())
                                    .requiredQuantity(item.getQuantity())
                                    .build())
                            .collect(Collectors.toList());
            
            // Publish product validation request
            ProductValidationRequestedEvent productValidationEvent = ProductValidationRequestedEvent.builder()
                    .requestId(order.getId().toString())
                    .productIds(request.getItems().stream()
                            .map(item -> item.getProductId().toString())
                            .collect(Collectors.toList()))
                    .requiredQuantities(productQuantities)
                    .requestingService("order-service")
                    .source("order-service")
                    .correlationId(order.getId().toString())
                    .build();
            
            eventPublisher.publish(productValidationEvent);
            log.info("Published ProductValidationRequestedEvent for order: {}", order.getId());
            
        } catch (Exception e) {
            log.error("Failed to publish validation request events for order: {}", order.getId(), e);
            // Don't fail the order creation if event publishing fails
        }
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
