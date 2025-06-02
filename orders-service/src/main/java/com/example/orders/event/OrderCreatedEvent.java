package com.example.orders.event;

import java.math.BigDecimal;

// PRODUCED by OrdersService
public record OrderCreatedEvent(
        String orderId,
        String userId,
        BigDecimal amount
) {}