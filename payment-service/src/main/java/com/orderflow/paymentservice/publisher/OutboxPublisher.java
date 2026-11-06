package com.orderflow.paymentservice.publisher;

import com.orderflow.paymentservice.entity.OutboxEvent;
import com.orderflow.paymentservice.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxPublisher {

    private final OutboxEventRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void publish() {
        var events = outboxRepository.findByPublishedFalseOrderByCreatedAt();

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
                                outboxRepository.save(event);
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
            case "PaymentCompleted" -> "payment.completed";
            case "PaymentFailed" -> "payment.failed";
            default -> throw new IllegalArgumentException("Unknown event type: " + eventType);
        };
    }
}