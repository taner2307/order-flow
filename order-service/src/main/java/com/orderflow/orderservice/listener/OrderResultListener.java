package com.orderflow.orderservice.listener;

import com.orderflow.common.event.PaymentCompletedEvent;
import com.orderflow.common.event.PaymentFailedEvent;
import com.orderflow.common.event.InventoryReservationFailedEvent;
import com.orderflow.orderservice.entity.Order;
import com.orderflow.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderResultListener {

    private final OrderRepository orderRepository;

    @KafkaListener(topics = "payment.completed", groupId = "order-service",
            properties = {
                    "spring.json.value.default.type=com.orderflow.common.event.PaymentCompletedEvent",
                    "spring.json.use.type.headers=false"
            })
    @Transactional
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        log.info("Received PaymentCompleted for order {}", event.orderId());
        updateOrder(event.orderId(), Order::markPaid);
    }

    @KafkaListener(topics = "payment.failed", groupId = "order-service",
            properties = {
                    "spring.json.value.default.type=com.orderflow.common.event.PaymentFailedEvent",
                    "spring.json.use.type.headers=false"
            })
    @Transactional
    public void handlePaymentFailed(PaymentFailedEvent event) {
        log.info("Received PaymentFailed for order {}", event.orderId());
        updateOrder(event.orderId(), order -> order.markFailed(event.reason()));
    }

    @KafkaListener(topics = "inventory.reservation-failed", groupId = "order-service",
            properties = {
                    "spring.json.value.default.type=com.orderflow.common.event.InventoryReservationFailedEvent",
                    "spring.json.use.type.headers=false"
            })
    @Transactional
    public void handleInventoryFailed(InventoryReservationFailedEvent event) {
        log.info("Received InventoryReservationFailed for order {}", event.orderId());
        updateOrder(event.orderId(), order -> order.markFailed(event.reason()));
    }

    private void updateOrder(UUID orderId, java.util.function.Consumer<Order> action) {
        orderRepository.findById(orderId).ifPresentOrElse(order -> {
            try {
                action.accept(order);
                orderRepository.save(order);
                log.info("Order {} updated to status {}", orderId, order.getStatus());
            } catch (IllegalStateException e) {
                log.warn("Could not update order {}: {}", orderId, e.getMessage());
            }
        }, () -> log.warn("Order not found: {}", orderId));
    }
}