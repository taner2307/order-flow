package com.orderflow.inventoryservice.listener;

import com.orderflow.common.event.OrderCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderCreatedListener {

    @KafkaListener(topics = "order.created", groupId = "inventory-service")
    public void handle(OrderCreatedEvent event) {
        log.info("Inventory Service received order: id={}, total={}, items={}",
                event.orderId(), event.totalAmount(), event.items().size());
    }
}
