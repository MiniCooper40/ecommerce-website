package com.ecommerce.order.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.ecommerce.order.dto.CreateOrderItemRequest;
import com.ecommerce.order.dto.CreateOrderRequest;
import com.ecommerce.order.dto.OrderDto;
import com.ecommerce.order.entity.OrderStatus;
import com.ecommerce.order.service.OrderService;
import com.ecommerce.shared.testutil.WithMockUserPrincipal;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
public class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUserPrincipal(userId = "test-user", roles = {"USER"})
    public void testGetUserOrders() throws Exception {
        // Arrange
        OrderDto order = createSampleOrder();
        when(orderService.getUserOrders("test-user")).thenReturn(Arrays.asList(order));

        // Act & Assert
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].userId").value("test-user"))
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    @WithMockUserPrincipal(userId = "test-user", roles = {"USER"})
    public void testGetOrder() throws Exception {
        // Arrange
        OrderDto order = createSampleOrder();
        when(orderService.getOrder(1L, "test-user")).thenReturn(order);

        // Act & Assert
        mockMvc.perform(get("/api/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.userId").value("test-user"))
                .andExpect(jsonPath("$.totalAmount").value(99.99));
    }

    @Test
    @WithMockUserPrincipal(userId = "test-user", roles = {"USER"})
    public void testCreateOrder() throws Exception {
        // Arrange
        CreateOrderRequest request = createSampleOrderRequest();
        OrderDto order = createSampleOrder();
        when(orderService.createOrder(any(CreateOrderRequest.class), anyString())).thenReturn(order);

        // Act & Assert
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.userId").value("test-user"));
    }

    @Test
    @WithMockUserPrincipal(userId = "test-user", roles = {"USER"})
    public void testCancelOrder() throws Exception {
        // Arrange
        OrderDto order = createSampleOrder();
        order.setStatus(OrderStatus.CANCELLED);
        when(orderService.cancelOrder(1L, "test-user")).thenReturn(order);

        // Act & Assert
        mockMvc.perform(put("/api/orders/1/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    @WithMockUserPrincipal(userId = "admin-user", roles = {"ADMIN"})
    public void testGetAllOrders_AdminUser() throws Exception {
        // Arrange
        OrderDto order = createSampleOrder();
        when(orderService.getAllOrders()).thenReturn(Arrays.asList(order));

        // Act & Assert
        mockMvc.perform(get("/api/orders/admin/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].userId").value("test-user"));
    }

    @Test
    @WithMockUserPrincipal(userId = "regular-user", roles = {"USER"})
    public void testGetAllOrders_RegularUser_Forbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/orders/admin/all"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testGetUserOrders_Unauthenticated() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testCreateOrder_Unauthenticated() throws Exception {
        // Arrange
        CreateOrderRequest request = createSampleOrderRequest();

        // Act & Assert
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    private OrderDto createSampleOrder() {
        OrderDto order = new OrderDto();
        order.setId(1L);
        order.setUserId("test-user");
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(new BigDecimal("99.99"));
        order.setCreatedAt(LocalDateTime.now());
        return order;
    }

    private CreateOrderRequest createSampleOrderRequest() {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setShippingAddress("123 Main St, Anytown, ST 12345");
        request.setBillingAddress("456 Oak Ave, Somewhere, ST 67890");
        
        // Create sample order items
        CreateOrderItemRequest item1 = new CreateOrderItemRequest();
        item1.setProductId(1L);
        item1.setQuantity(2);
        
        CreateOrderItemRequest item2 = new CreateOrderItemRequest();
        item2.setProductId(2L);
        item2.setQuantity(1);
        
        request.setItems(Arrays.asList(item1, item2));
        return request;
    }
}