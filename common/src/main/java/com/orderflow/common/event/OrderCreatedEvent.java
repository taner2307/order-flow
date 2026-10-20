package com.orderflow.common.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true) //İleride event’e yeni alan eklendiğinde eski tüketiciler kırılmasın
public record OrderCreatedEvent(
        UUID orderId,
        String customerId,
        BigDecimal totalAmount,
        List<OrderItemDto> items,
        LocalDateTime createdAt
) {
    public record OrderItemDto(
            String productId,
            Integer quantity,
            BigDecimal price
    ) {}
}
