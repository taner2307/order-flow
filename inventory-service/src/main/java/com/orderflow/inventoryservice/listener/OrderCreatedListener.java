package com.orderflow.inventoryservice.listener;

import com.orderflow.common.event.InventoryReservationFailedEvent;
import com.orderflow.common.event.InventoryReservedEvent;
import com.orderflow.common.event.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCreatedListener {

    private final KafkaTemplate<String,Object> kafkaTemplate;

    @KafkaListener(topics = "order.created", groupId = "inventory-service")
    public void handle(OrderCreatedEvent event) {
        log.info("Processing inventory reservation for order: {}", event.orderId());

        boolean hasUnAvailableProduct = event.items().stream()
                .anyMatch(item -> "PROD-999".equals(item.productId()));

        if(hasUnAvailableProduct) {
            InventoryReservationFailedEvent failedEvent = new InventoryReservationFailedEvent(
                    event.orderId(),
                    "Product PROD-999 is out of stock",
                    java.time.LocalDateTime.now()
            );
            kafkaTemplate.send("inventory.reservation-failed", event.orderId().toString(), failedEvent);
            log.info("Published InventoryReservationFailed for order {}", event.orderId());
        }else {
            InventoryReservedEvent reservedEvent = new InventoryReservedEvent(
                    event.orderId(),
                    event.totalAmount(),
                    LocalDateTime.now()
            );
            kafkaTemplate.send("inventory.reserved", event.orderId().toString(), reservedEvent);
            log.info("Published InventoryReserved for order {}", event.orderId());
        }
    }
}
