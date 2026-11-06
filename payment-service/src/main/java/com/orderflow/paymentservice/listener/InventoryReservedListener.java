package com.orderflow.paymentservice.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.orderflow.common.event.InventoryReservedEvent;
import com.orderflow.common.event.PaymentCompletedEvent;
import com.orderflow.common.event.PaymentFailedEvent;
import com.orderflow.paymentservice.entity.OutboxEvent;
import com.orderflow.paymentservice.repository.OutboxEventRepository;
import com.orderflow.paymentservice.repository.PaymentRepository;
import com.orderflow.paymentservice.entity.Payment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryReservedListener {

    private final PaymentRepository paymentRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "inventory.reserved", groupId = "payment-service",properties = {
            "spring.json.value.default.type=com.orderflow.common.event.InventoryReservedEvent",
            "spring.json.use.type.headers=false"
    })
    public void handle(InventoryReservedEvent event) {
        log.info("Starting payment for order {} with amount {}",
                event.orderId(), event.totalAmount());

        Payment payment = Payment.create(event.orderId(), event.totalAmount());
        paymentRepository.save(payment);

        boolean paymentSuccessful = event.totalAmount().compareTo(new BigDecimal("1000")) < 0;

        String eventType;
        Object eventPayload;

        if (paymentSuccessful) {
            payment.markCompleted();
            eventType = "PaymentCompleted";
            eventPayload = new PaymentCompletedEvent(
                    event.orderId(),
                    event.totalAmount(),
                    LocalDateTime.now()
            );
        } else {
            payment.markFailed();
            eventType = "PaymentFailed";
            eventPayload = new PaymentFailedEvent(event.orderId(), "Amount exceeds limit", LocalDateTime.now());
        }
        paymentRepository.save(payment);

        OutboxEvent outboxEvent = OutboxEvent.create(
                "Payment",
                event.orderId().toString(),
                eventType,
                toJson(eventPayload)
        );

        outboxEventRepository.save(outboxEvent);
        log.info("Payment and OutboxEvent saved for order {}", event.orderId());
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
