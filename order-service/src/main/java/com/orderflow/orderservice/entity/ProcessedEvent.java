package com.orderflow.orderservice.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "processed_events")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProcessedEvent {

    @Id
    private String eventId;

    private LocalDateTime processedAt;

    public static ProcessedEvent of(String eventId) {
        ProcessedEvent event = new ProcessedEvent();
        event.eventId = eventId;
        event.processedAt = LocalDateTime.now();
        return event;
    }
}
