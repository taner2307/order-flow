package com.orderflow.inventoryservice.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "reservations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID orderId;

    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    private LocalDateTime createdAt;

    public static Reservation create(UUID orderId) {
        Reservation reservation = new Reservation();
        reservation.orderId = orderId;
        reservation.status = ReservationStatus.PENDING;
        reservation.createdAt = LocalDateTime.now();
        return reservation;
    }

    public void markReserved() {
        if (status != ReservationStatus.PENDING) {
            throw new IllegalStateException("Only PENDING reservation can be reserved");
        }
        this.status = ReservationStatus.RESERVED;
    }

    public void markFailed() {
        if (status != ReservationStatus.PENDING) {
            throw new IllegalStateException("Only PENDING reservation can be failed");
        }
        this.status = ReservationStatus.FAILED;
    }
}
