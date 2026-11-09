package com.orderflow.orderservice.entity;

public enum OrderStatus {
    NEW,
    PAYMENT_PENDING,
    INVENTORY_RESERVED,   // stok ayrıldı
    PAID,                 // ödeme alındı
    COMPLETED,            // her şey bitti
    COMPENSATING,         // telafi ediliyor
    FAILED,
    CANCELLED
}
