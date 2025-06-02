package com.example.orders.dto;

import com.example.orders.model.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List; // Assuming items might be expanded in response

public record OrderResponse(
        String id,
        String userId,
        List<OrderItemDto> items,
        BigDecimal totalAmount,
        OrderStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}