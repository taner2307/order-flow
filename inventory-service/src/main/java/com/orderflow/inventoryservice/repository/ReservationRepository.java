package com.orderflow.inventoryservice.repository;

import com.orderflow.inventoryservice.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ReservationRepository extends JpaRepository<Reservation, UUID> {
}
