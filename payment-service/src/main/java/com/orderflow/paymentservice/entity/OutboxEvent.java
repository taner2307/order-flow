package com.orderflow.paymentservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "outbox_events")
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String aggregateType;
    private String aggregateId;
    private String type;
    private String payload;

    private LocalDateTime createdAt;
    private boolean published = false;

    public static OutboxEvent create(String aggregateType, String aggregateId, String type, String payload) {
        OutboxEvent event = new OutboxEvent();
        event.aggregateType = aggregateType;
        event.aggregateId = aggregateId;
        event.type = type;
        event.payload = payload;
        event.createdAt = LocalDateTime.now();
        return event;
    }

    public void markAsPublished() {
        this.published = true;
    }

}
