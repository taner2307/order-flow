package com.orderflow.inventoryservice.publisher;

import com.orderflow.inventoryservice.entity.OutboxEvent;
import com.orderflow.inventoryservice.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
@RequiredArgsConstructor
public class OutboxPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final OutboxEventRepository outboxEventRepository;

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void publish() {
        var events = outboxEventRepository.findByPublishedFalseOrderByCreatedAt();

        if (events.isEmpty()) {
            return;
        }
        log.info("Found {} unpublished outbox events", events.size());

        for (OutboxEvent event : events) {
            try {
                String topic = resolveTopic(event.getType());
                kafkaTemplate.send(topic, event.getAggregateId(), event.getPayload())
                        .whenComplete((result, ex) -> {
                            if (ex == null) {
                                event.markAsPublished();
                                outboxEventRepository.save(event);
                                log.info("Published outbox event: {} for aggregate {}",
                                        event.getType(), event.getAggregateId());
                            } else {
                                log.error("Failed to publish outbox event: {}", event.getId(), ex);
                            }
                        });
            } catch (Exception e) {
                log.error("Error processing outbox event {}", event.getId(), e);
            }
        }
    }

    private String resolveTopic(String eventType) {
        return switch (eventType) {
            case "InventoryReserved" -> "inventory.reserved";
            case "InventoryReservationFailed" -> "inventory.reservation-failed";
            default -> throw new IllegalArgumentException("Unknown event type: " + eventType);
        };
    }

}
