package com.example.orders.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record OrderItemDto(
        @NotBlank String productId,
        @NotNull @Min(1) Integer quantity,
        @NotNull BigDecimal pricePerUnit
) {}