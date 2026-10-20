package com.orderflow.paymentservice.listener;

import com.orderflow.common.event.OrderCreatedEvent;
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
public class OrderCreatedListener {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(topics = "order.created", groupId = "payment-service")
    public void handle(OrderCreatedEvent event) {
        log.info("Processing payment for order: {}, amount: {}",
                event.orderId(), event.totalAmount());

        // Test kuralı: 1000 ve üzeri başarısız
        boolean paymentSuccessful = event.totalAmount().compareTo(new BigDecimal("1000")) < 0;
        if(!paymentSuccessful) {
            log.error("Payment failed for order: {}, amount: {}", event.orderId(), event.totalAmount());
            PaymentFailedEvent failed = new PaymentFailedEvent(
                    event.orderId(), "Amount exceeds limit", LocalDateTime.now());
            kafkaTemplate.send("payment.failed",event.orderId().toString(), failed);
            log.info("Published PaymentFailed for order {}", event.orderId());
        }else {
            PaymentCompletedEvent completed = new PaymentCompletedEvent(
                    event.orderId(),
                    event.totalAmount(),
                    LocalDateTime.now()
            );
            kafkaTemplate.send("payment.completed", event.orderId().toString(), completed);
            log.info("Published PaymentCompleted for order {}", event.orderId());
        }
    }
}
