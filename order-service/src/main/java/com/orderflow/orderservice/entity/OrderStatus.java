package com.orderflow.orderservice.entity;

public enum OrderStatus {
    NEW,
    PAYMENT_PENDING,
    PAID,
    RESERVED,
    COMPLETED,
    CANCELLED,
    FAILED
}
