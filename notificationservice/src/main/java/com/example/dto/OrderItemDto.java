package com.example.dto;

import java.math.BigDecimal;

public record OrderItemDto(
        String productId,
        Integer quantity,
        BigDecimal pricePerUnit
) {}