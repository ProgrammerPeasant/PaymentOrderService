package com.example.model;

import com.example.event.PaymentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payments_transactions", indexes = {
        @Index(name = "idx_payment_order_id", columnList = "orderId", unique = true)
})
@Getter @Setter @NoArgsConstructor
public class PaymentTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private String orderId;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    private String failureReason;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public PaymentTransaction(String orderId, String userId, BigDecimal amount, PaymentStatus status, String failureReason) {
        this.orderId = orderId;
        this.userId = userId;
        this.amount = amount;
        this.status = status;
        this.failureReason = failureReason;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}