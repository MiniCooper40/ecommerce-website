package com.ecommerce.order.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ecommerce.order.dto.OrderDto;
import com.ecommerce.order.dto.OrderItemDto;
import com.ecommerce.order.entity.Order;
import com.ecommerce.order.entity.OrderItem;

/**
 * Mapper component for converting between Order entities and DTOs
 * Following the common Spring pattern of having dedicated mapper beans
 */
@Component
public class OrderMapper {

    @Autowired
    private AddressMapper addressMapper;

    /**
     * Convert Order entity to OrderDto
     * @param order The order entity to convert
     * @return OrderDto or null if input is null
     */
    public OrderDto toDto(Order order) {
        if (order == null) {
            return null;
        }

        List<OrderItemDto> itemDtos = order.getItems() != null 
                ? order.getItems().stream()
                    .map(this::toItemDto)
                    .collect(Collectors.toList())
                : List.of();

        return new OrderDto(
                order.getId(),
                order.getUserId(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getCartValidated(),
                order.getStockValidated(),
                order.getValidationCompletedAt(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                addressMapper.toDto(order.getShippingAddress()),
                addressMapper.toDto(order.getBillingAddress()),
                itemDtos
        );
    }

    /**
     * Convert OrderItem entity to OrderItemDto
     * @param item The order item entity to convert
     * @return OrderItemDto or null if input is null
     */
    public OrderItemDto toItemDto(OrderItem item) {
        if (item == null) {
            return null;
        }

        return new OrderItemDto(
                item.getId(),
                item.getProductId(),
                item.getProductName(),
                item.getProductImageUrl(),
                item.getQuantity(),
                item.getPrice(),
                item.getSubtotal()
        );
    }

    /**
     * Convert list of Order entities to list of OrderDtos
     * @param orders The list of order entities to convert
     * @return List of OrderDto
     */
    public List<OrderDto> toDtoList(List<Order> orders) {
        if (orders == null) {
            return List.of();
        }

        return orders.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}
