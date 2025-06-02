package com.example.event;

import java.math.BigDecimal;

public record PaymentProcessedEvent(
        String orderId,
        String userId,
        String paymentId,
        PaymentStatus status,
        BigDecimal amountProcessed,
        String reason
) {}