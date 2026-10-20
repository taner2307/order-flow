package com.orderflow.orderservice.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String customerId;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<OrderItem> items = new ArrayList<>();

    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private LocalDateTime createdAt;

    public static Order create(String customerId, List<OrderItem> items) {
        if(customerId == null || customerId.isEmpty()) {
            throw new IllegalArgumentException("Customer ID cannot be null or empty");
        }

        if(items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }

        Order order = new Order();
        order.customerId = customerId;
        order.status = OrderStatus.NEW;
        order.createdAt = LocalDateTime.now();
        items.forEach(order::addItem);
        order.recalculateTotal();

        return order;
    }

    private void addItem(OrderItem item) {
        item.assignTo(this);
        items.add(item);
    }

    private void recalculateTotal() {
        this.totalAmount = items.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void markPaymentPending() {
        if (this.status != OrderStatus.NEW) {
            throw new IllegalStateException(
                    "Only NEW orders can be moved to PAYMENT_PENDING. Current: " + status);
        }
        this.status = OrderStatus.PAYMENT_PENDING;
    }

    public void markPaid() {
        if (this.status != OrderStatus.PAYMENT_PENDING) {
            throw new IllegalStateException(
                    "Only PAYMENT_PENDING orders can be marked as PAID. Current: " + status);
        }
        this.status = OrderStatus.PAID;
    }

    public void markFailed(String reason) {
        this.status = OrderStatus.FAILED;
    }

    public void cancel() {
        if (this.status == OrderStatus.COMPLETED) {
            throw new IllegalStateException("Completed order cannot be cancelled");
        }
        this.status = OrderStatus.CANCELLED;
    }
}
