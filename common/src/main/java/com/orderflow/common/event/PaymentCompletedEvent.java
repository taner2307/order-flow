package com.orderflow.common.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PaymentCompletedEvent(
        UUID orderId,
        BigDecimal amount,
        LocalDateTime completedAt
) {
}
