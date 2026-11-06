package com.orderflow.inventoryservice.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.orderflow.common.event.InventoryReservationFailedEvent;
import com.orderflow.common.event.InventoryReservedEvent;
import com.orderflow.common.event.OrderCreatedEvent;
import com.orderflow.inventoryservice.entity.OutboxEvent;
import com.orderflow.inventoryservice.entity.Reservation;
import com.orderflow.inventoryservice.repository.OutboxEventRepository;
import com.orderflow.inventoryservice.repository.ReservationRepository;
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

    private final ObjectMapper objectMapper;
    private final ReservationRepository reservationRepository;
    private final OutboxEventRepository outboxEventRepository;

    @KafkaListener(topics = "order.created", groupId = "inventory-service")
    public void handle(OrderCreatedEvent event) {
        log.info("Processing inventory reservation for order: {}", event.orderId());

        Reservation reservation = Reservation.create(event.orderId());
        reservationRepository.save(reservation);

        boolean outOfStock = event.items().stream()
                .anyMatch(item -> "PROD-999".equals(item.productId()));

        String eventType;
        Object payload;

        if(outOfStock) {
            reservation.markFailed();
            eventType = "InventoryReservationFailedEvent";
            payload = new InventoryReservationFailedEvent(
                    event.orderId(),
                    "Product PROD-999 is out of stock",
                    LocalDateTime.now()
            );
        }else {
            reservation.markReserved();
            eventType = "InventoryReserved";
            payload = new InventoryReservedEvent(
                    event.orderId(),
                    event.totalAmount(),
                    LocalDateTime.now()
            );
        }
        reservationRepository.save(reservation);

        OutboxEvent outboxEvent = OutboxEvent.create(
                "Reservation",
                reservation.getId().toString(),
                eventType,
                toJson(payload)
        );

        outboxEventRepository.save(outboxEvent);
        log.info("Reservation and OutboxEvent saved for order {}", event.orderId());
    }

    private String toJson(Object obj) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
