package com.orderflow.orderservice.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "order_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String productId;
    private Integer quantity;
    private BigDecimal unitPrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    public static OrderItem create(String productId, Integer quantity, BigDecimal unitPrice) {
        if(productId == null || productId.isEmpty()) {
            throw new IllegalArgumentException("Product ID cannot be null or empty");
        }
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Unit price must be positive");
        }

        OrderItem orderItem = new OrderItem();
        orderItem.productId = productId;
        orderItem.quantity = quantity;
        orderItem.unitPrice= unitPrice;

        return orderItem;
    }

    public BigDecimal getSubtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    void assignTo(Order order) {
        this.order = order;
    }

}
