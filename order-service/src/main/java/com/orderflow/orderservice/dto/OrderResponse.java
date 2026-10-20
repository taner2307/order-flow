package com.orderflow.orderservice.dto;

import com.orderflow.orderservice.entity.Order;
import com.orderflow.orderservice.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        String customerId,
        BigDecimal totalAmount,
        OrderStatus status,
        LocalDateTime createdAt,
        List<ItemResponse> items
) {
    public record ItemResponse(String productId, Integer quantity, BigDecimal unitPrice) {}

    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getCustomerId(),
                order.getTotalAmount(),
                order.getStatus(),
                order.getCreatedAt(),
                order.getItems().stream()
                        .map(i -> new ItemResponse(i.getProductId(), i.getQuantity(), i.getUnitPrice()))
                        .toList()
        );
    }
}