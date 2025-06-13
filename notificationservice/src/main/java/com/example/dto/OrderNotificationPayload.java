package com.example.dto;

public record OrderNotificationPayload(
        String id,
        String userId,
        java.math.BigDecimal totalAmount,
        String status
) {}