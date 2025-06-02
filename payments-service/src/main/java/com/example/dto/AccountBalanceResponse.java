package com.example.dto;

import java.math.BigDecimal;

public record AccountBalanceResponse(
        String userId,
        BigDecimal balance
) {}