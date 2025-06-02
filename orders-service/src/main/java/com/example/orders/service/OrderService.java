package com.example.orders.service;

import com.example.orders.dto.CreateOrderRequest;
import com.example.orders.dto.OrderItemDto;
import com.example.orders.dto.OrderResponse;
import com.example.orders.event.OrderCreatedEvent;
import com.example.orders.event.PaymentProcessedEvent;
import com.example.orders.model.Order;
import com.example.orders.model.OrderStatus;
import com.example.orders.model.OutboxMessage;
import com.example.orders.repository.OrderRepository;
import com.example.orders.repository.OutboxMessageRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    private final OrderRepository orderRepository;
    private final OutboxMessageRepository outboxMessageRepository;
    private final ObjectMapper objectMapper;

    @Value("${app.kafka.order-created-topic}")
    private String orderCreatedTopic;

    @Transactional
    public OrderResponse createOrder(String userId, CreateOrderRequest request) {
        BigDecimal totalAmount = request.items().stream()
                .map(item -> item.pricePerUnit().multiply(BigDecimal.valueOf(item.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        String itemsJson;
        try {
            itemsJson = objectMapper.writeValueAsString(request.items());
        } catch (JsonProcessingException e) {
            log.error("Error serializing order items for user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Could not process order items.", e);
        }

        Order order = new Order(userId, itemsJson, totalAmount);
        order.setStatus(OrderStatus.CREATED);
        Order savedOrder = orderRepository.save(order);

        OrderCreatedEvent event = new OrderCreatedEvent(
                savedOrder.getId(),
                savedOrder.getUserId(),
                savedOrder.getTotalAmount()
        );

        try {
            String payloadJson = objectMapper.writeValueAsString(event);
            OutboxMessage outboxMessage = new OutboxMessage(
                    "Order",
                    savedOrder.getId(),
                    orderCreatedTopic,
                    savedOrder.getId(),
                    payloadJson
            );
            outboxMessageRepository.save(outboxMessage);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize OrderCreatedEvent for order {}", savedOrder.getId(), e);
            throw new RuntimeException("Serialization error for order created event", e);
        }

        log.info("Order {} created for user {}. Event placed in outbox.", savedOrder.getId(), userId);
        return mapToOrderResponse(savedOrder);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersForUser(String userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<OrderResponse> getOrderByIdAndUser(String orderId, String userId) {
        return orderRepository.findByIdAndUserId(orderId, userId)
                .map(this::mapToOrderResponse);
    }

    @Transactional
    public void updateOrderStatus(PaymentProcessedEvent event) {
        log.info("Processing payment event for order ID: {}", event.orderId());
        Optional<Order> orderOptional = orderRepository.findById(event.orderId());

        if (orderOptional.isEmpty()) {
            log.warn("Order with ID {} not found for payment event.", event.orderId());
            return;
        }

        Order order = orderOptional.get();

        if (order.getStatus() == OrderStatus.PAID || order.getStatus() == OrderStatus.PAYMENT_FAILED) {
            if ((order.getStatus() == OrderStatus.PAID && event.status() == com.example.orders.event.PaymentStatus.SUCCESS) ||
                    (order.getStatus() == OrderStatus.PAYMENT_FAILED && event.status() != com.example.orders.event.PaymentStatus.SUCCESS)) {
                log.info("Order {} already in terminal state {} consistent with payment event. Skipping update.",
                        order.getId(), order.getStatus());
                return;
            }
            log.warn("Order {} is in terminal state {} but received conflicting payment event status {}. Manual check might be needed.",
                    order.getId(), order.getStatus(), event.status());
        }


        switch (event.status()) {
            case SUCCESS:
                order.setStatus(OrderStatus.PAID);
                log.info("Order {} marked as PAID.", order.getId());
                break;
            case FAILURE_INSUFFICIENT_FUNDS:
            case FAILURE_ACCOUNT_NOT_FOUND:
            case FAILURE_ORDER_ALREADY_PROCESSED:
            case FAILURE_OTHER:
                order.setStatus(OrderStatus.PAYMENT_FAILED);
                log.warn("Order {} payment failed. Reason: {}. Marked as PAYMENT_FAILED.", order.getId(), event.reason());
                break;
            default:
                log.warn("Unhandled payment status {} for order {}.", event.status(), order.getId());
                return;
        }
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
    }


    private OrderResponse mapToOrderResponse(Order order) {
        List<OrderItemDto> items = null;
        if (order.getItemsJson() != null && !order.getItemsJson().isBlank()) {
            try {
                items = objectMapper.readValue(order.getItemsJson(), new TypeReference<List<OrderItemDto>>() {});
            } catch (JsonProcessingException e) {
                log.error("Error deserializing itemsJson for order {}: {}", order.getId(), e.getMessage());
            }
        }
        return new OrderResponse(
                order.getId(),
                order.getUserId(),
                items,
                order.getTotalAmount(),
                order.getStatus(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
}