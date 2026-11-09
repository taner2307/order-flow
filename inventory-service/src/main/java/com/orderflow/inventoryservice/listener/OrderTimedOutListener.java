package com.orderflow.inventoryservice.listener;

import com.orderflow.common.event.OrderTimedOutEvent;
import com.orderflow.inventoryservice.entity.ReservationStatus;
import com.orderflow.inventoryservice.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTimedOutListener {

    private final ReservationRepository reservationRepository;

    @KafkaListener(
            topics = "order.timed-out",
            groupId = "inventory-service",
            properties = {
                    "spring.json.value.default.type=com.orderflow.common.event.OrderTimedOutEvent",
                    "spring.json.use.type.headers=false"
            }
    )
    @Transactional
    public void handleTimeout(OrderTimedOutEvent event) {
        log.info("Order timed out, releasing reservation: {}", event.orderId());

        reservationRepository.findByOrderId(event.orderId())
                .ifPresent(reservation -> {
                    if (reservation.getStatus() == ReservationStatus.RESERVED) {
                        reservation.markReleased();
                        reservationRepository.save(reservation);
                        log.info("Reservation released due to timeout: {}", event.orderId());
                    }
                });
    }

}
