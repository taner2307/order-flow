package com.orderflow.orderservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;

public record CreateOrderRequest(
        @NotBlank String customerId,
        @NotEmpty @Valid List<OrderItemRequest> items
) {
    public record OrderItemRequest(
            @NotBlank String productId,
            @Positive Integer quantity,
            @Positive BigDecimal unitPrice
    ) {}
}
