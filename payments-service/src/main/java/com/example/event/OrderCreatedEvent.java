package com.example.event;

import java.math.BigDecimal;

public record OrderCreatedEvent(
        String orderId,
        String userId,
        BigDecimal amount
) {}