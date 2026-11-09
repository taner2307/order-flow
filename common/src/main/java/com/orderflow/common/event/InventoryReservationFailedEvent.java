package com.orderflow.common.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record InventoryReservationFailedEvent(
        UUID orderId,
        String reason,
        LocalDateTime failedAt
) {
}
