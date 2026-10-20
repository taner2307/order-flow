package com.orderflow.paymentservice.listener;

import com.orderflow.common.event.InventoryReservedEvent;
import com.orderflow.common.event.PaymentCompletedEvent;
import com.orderflow.common.event.PaymentFailedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryReservedListener {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(topics = "inventory.reserved", groupId = "payment-service")
    public void handle(InventoryReservedEvent event) {
        log.info("Starting payment for order {} with amount {}",
                event.orderId(), event.totalAmount());

        boolean paymentSuccessful = event.totalAmount().compareTo(new BigDecimal("1000")) < 0;

        if (paymentSuccessful) {
            var completed = new PaymentCompletedEvent(
                    event.orderId(),
                    event.totalAmount(),
                    LocalDateTime.now()
            );
            kafkaTemplate.send("payment.completed", event.orderId().toString(), completed);
            log.info("PaymentCompleted published for order {}", event.orderId());
        } else {
            var failed = new PaymentFailedEvent(
                    event.orderId(),
                    "Amount exceeds limit",
                    LocalDateTime.now()
            );
            kafkaTemplate.send("payment.failed", event.orderId().toString(), failed);
            log.info("PaymentFailed published for order {}", event.orderId());
        }

    }

}
