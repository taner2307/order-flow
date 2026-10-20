package com.orderflow.common.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentCompletedEvent(
        UUID orderId,
        BigDecimal amount,
        LocalDateTime completedAt
) {
}
