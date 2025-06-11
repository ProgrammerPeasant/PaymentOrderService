package com.example.dto;

import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonProperty;

public record OrderEventPayload(

        @JsonProperty("orderId") String orderId,
        @JsonProperty("userId") String userId,
        @JsonProperty("amount") BigDecimal amount
) {}