package com.example.orders.event;

import java.math.BigDecimal;

// CONSUMED by OrdersService
public record PaymentProcessedEvent(
        String orderId,
        String userId,
        String paymentId,
        PaymentStatus status,
        BigDecimal amountProcessed,
        String reason
) {}