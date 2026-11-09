package com.orderflow.orderservice.scheduler;

import com.orderflow.common.event.OrderTimedOutEvent;
import com.orderflow.orderservice.entity.Order;
import com.orderflow.orderservice.entity.OrderStatus;
import com.orderflow.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTimeoutScheduler {

    private final OrderRepository orderRepository;
    private final KafkaTemplate<String,OrderTimedOutEvent> kafkaTemplate;

    @Scheduled(fixedDelay = 30000)
    @Transactional
    public void cancelTimedOutOrders() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(2);

        List<Order> timedOut = orderRepository
                .findByStatusAndCreatedAtBefore(OrderStatus.PAYMENT_PENDING, threshold);

        if (timedOut.isEmpty()) {
            return;
        }

        log.info("Found {} timed-out orders", timedOut.size());

        for (Order order : timedOut) {
            order.markFailed("Payment timeout");
            orderRepository.save(order);

            OrderTimedOutEvent event = new OrderTimedOutEvent(order.getId(), LocalDateTime.now());
            kafkaTemplate.send("order.timed-out", order.getId().toString(), event);

            log.info("Order {} timed out and event published", order.getId());

        }
    }
}
