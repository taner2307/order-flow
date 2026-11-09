package com.orderflow.orderservice.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "idempotency_keys")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IdempotencyKey {

    @Id
    private String key;

    private UUID orderId;

    private LocalDateTime createdAt;

    public static IdempotencyKey create(String key, UUID orderId) {
        IdempotencyKey entity = new IdempotencyKey();
        entity.key = key;
        entity.orderId = orderId;
        entity.createdAt = LocalDateTime.now();
        return entity;
    }
}
