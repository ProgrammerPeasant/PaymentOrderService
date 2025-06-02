package com.example.orders.model;

public enum OrderStatus {
    CREATED,
    PAYMENT_PENDING,
    PAID,
    PAYMENT_FAILED,
    SHIPPED,        // эти на всякий случай добавил но нне использовал пока что
    DELIVERED,
    CANCELLED
}