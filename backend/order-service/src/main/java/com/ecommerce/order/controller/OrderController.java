package com.ecommerce.order.controller;

import com.ecommerce.order.dto.OrderDto;
import com.ecommerce.order.dto.CreateOrderRequest;
import com.ecommerce.order.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping
    public ResponseEntity<List<OrderDto>> getUserOrders(@RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(orderService.getUserOrders(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDto> getOrder(@PathVariable Long id, @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(orderService.getOrder(id, userId));
    }

    @PostMapping
    public ResponseEntity<OrderDto> createOrder(@Valid @RequestBody CreateOrderRequest request, 
                                               @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(orderService.createOrder(request, userId));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<OrderDto> cancelOrder(@PathVariable Long id, @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(orderService.cancelOrder(id, userId));
    }
}