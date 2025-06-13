package com.example.dto;

public record OrderNotificationPayload(
        String id, // Если в JSON поле "id"
        String userId,
        java.math.BigDecimal totalAmount, // Если totalAmount приходит и нужен
        String status
) {}