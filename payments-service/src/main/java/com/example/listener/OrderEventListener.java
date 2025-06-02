package com.example.listener;

import com.example.event.OrderCreatedEvent;
import com.example.service.PaymentService;
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
public class OrderEventListener {

    private final PaymentService paymentService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${app.kafka.order-created-topic}", groupId = "payments-group")
    @Transactional
    public void handleOrderCreatedEvent(ConsumerRecord<String, String> record) {
        log.info("Received OrderCreatedEvent: key={}, value={}", record.key(), record.value());
        try {
            OrderCreatedEvent event = objectMapper.readValue(record.value(), OrderCreatedEvent.class);
            paymentService.processOrderPayment(event, record.value());
        } catch (JsonProcessingException e) {
            log.error("Could not deserialize OrderCreatedEvent: {}", record.value(), e);
        } catch (Exception e) {
            log.error("Error processing OrderCreatedEvent for order {}: {}", record.key(), e.getMessage(), e);
            throw e;
        }
    }
}