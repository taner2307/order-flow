package com.orderflow.orderservice.service;

import com.orderflow.orderservice.dto.CreateOrderRequest;
import com.orderflow.orderservice.entity.Order;
import com.orderflow.orderservice.entity.OrderItem;
import com.orderflow.orderservice.repository.OrderRepository;
import com.orderflow.common.event.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;

    @Transactional
    public Order createOrder(CreateOrderRequest request) {

        List<OrderItem> items = request.items().stream()
                .map(i -> OrderItem.create(i.productId(), i.quantity(), i.unitPrice()))
                .toList();

        Order order = Order.create(request.customerId(), items);

        Order savedOrder = orderRepository.save(order);

        publishEvent(savedOrder);
        return savedOrder;
    }

    private void publishEvent(Order order) {
        OrderCreatedEvent event = new OrderCreatedEvent(
                order.getId(),
                order.getCustomerId(),
                order.getTotalAmount(),
                order.getItems().stream()
                        .map(i -> new OrderCreatedEvent.OrderItemDto(
                                i.getProductId(),
                                i.getQuantity(),
                                i.getUnitPrice()))
                        .toList(),
                order.getCreatedAt()
        );

        kafkaTemplate.send("order.created", order.getId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("OrderCreatedEvent publish failed. orderId={}", order.getId(), ex);
                    } else {
                        log.info("OrderCreatedEvent published. orderId={}", order.getId());
                    }
                });
    }
}