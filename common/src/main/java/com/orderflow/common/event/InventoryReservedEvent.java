package com.orderflow.common.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record InventoryReservedEvent(
        UUID orderId,
        LocalDateTime reservedAt
) {
}
