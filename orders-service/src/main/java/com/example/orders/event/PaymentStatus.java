package com.example.orders.event;

public enum PaymentStatus {
    SUCCESS,
    FAILURE_INSUFFICIENT_FUNDS,
    FAILURE_ACCOUNT_NOT_FOUND,
    FAILURE_ORDER_ALREADY_PROCESSED,
    FAILURE_OTHER
}