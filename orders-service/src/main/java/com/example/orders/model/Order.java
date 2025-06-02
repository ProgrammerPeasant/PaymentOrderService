package com.example.orders.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders_table") // "order" is a reserved keyword in SQL
@Getter @Setter @NoArgsConstructor
public class Order {
    @Id
    private String id;

    @Column(nullable = false)
    private String userId;

    @Column(columnDefinition = "TEXT")
    private String itemsJson;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(updatable = false)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Order(String userId, String itemsJson, BigDecimal totalAmount) {
        this.id = UUID.randomUUID().toString();
        this.userId = userId;
        this.itemsJson = itemsJson;
        this.totalAmount = totalAmount;
        this.status = OrderStatus.CREATED;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}