package com.orderflow.common.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record InventoryReservationFailedEvent(
        UUID orderId,
        String reason,
        LocalDateTime failedAt
) {
}
