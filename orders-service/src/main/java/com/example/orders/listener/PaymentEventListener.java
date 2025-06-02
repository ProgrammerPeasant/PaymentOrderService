package com.example.orders.listener;

import com.example.orders.event.PaymentProcessedEvent;
import com.example.orders.service.OrderService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventListener {

    private final OrderService orderService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${app.kafka.payment-processed-topic}", groupId = "orders-group")
    @Transactional
    public void handlePaymentProcessedEvent(ConsumerRecord<String, String> record) {
        log.info("Received PaymentProcessedEvent: key={}, value={}", record.key(), record.value());
        try {
            PaymentProcessedEvent event = objectMapper.readValue(record.value(), PaymentProcessedEvent.class);
            orderService.updateOrderStatus(event);
        } catch (JsonProcessingException e) {
            log.error("Could not deserialize PaymentProcessedEvent: {}", record.value(), e);
        } catch (Exception e) {
            log.error("Error processing PaymentProcessedEvent for order {}: {}", record.key(), e.getMessage(), e);
            throw e;
        }
    }
}