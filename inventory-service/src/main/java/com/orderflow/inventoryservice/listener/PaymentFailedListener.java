package com.orderflow.inventoryservice.listener;

import com.orderflow.common.event.PaymentFailedEvent;
import com.orderflow.inventoryservice.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentFailedListener {

    private final ReservationRepository reservationRepository;

    @KafkaListener(
            topics = "payment.failed",
            groupId = "inventory-service",
            properties = {
                    "spring.json.value.default.type=com.orderflow.common.event.PaymentFailedEvent",
                    "spring.json.use.type.headers=false"
            }
    )
    @Transactional
    public void handle(PaymentFailedEvent event) {
        log.info("Payment failed for order {}, releasing reservation", event.orderId());
        reservationRepository.findByOrderId(event.orderId())
                .ifPresentOrElse(reservation -> {
                    reservation.markReleased();
                    reservationRepository.save(reservation);
                    log.info("Reservation released for order {}", event.orderId());
                }, () -> log.warn("No reservation found for order {}", event.orderId()));
    }
}
