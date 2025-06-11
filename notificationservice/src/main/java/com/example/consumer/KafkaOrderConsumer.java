package com.example.consumer;

import com.example.dto.OrderEventPayload;
import com.example.handler.OrderStatusUpdateHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class KafkaOrderConsumer {

    private final OrderStatusUpdateHandler orderStatusUpdateHandler;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${kafka.topic.orderStatusUpdates:order-status-updates}", // Убедитесь, что топик правильный
            groupId = "${kafka.consumer.groupId:notification-group}")
    public void listenOrderUpdates(String messagePayload) {
        log.info("Received message from Kafka: {}", messagePayload);
        try {
            OrderEventPayload eventPayload = objectMapper.readValue(messagePayload, OrderEventPayload.class); // ИЗМЕНЕНО
            log.info("Deserialized to OrderEventPayload: {}", eventPayload);

            if (eventPayload.userId() == null || eventPayload.userId().trim().isEmpty()) {
                log.warn("Received order update without userId, cannot send WebSocket notification. Payload: {}", messagePayload);
                return;
            }

            // ПЕРЕДАЕМ НОВЫЙ DTO В ОБРАБОТЧИК
            orderStatusUpdateHandler.sendOrderStatusUpdate(eventPayload.userId(), eventPayload); // ИЗМЕНЕНО

        } catch (JsonProcessingException e) {
            log.error("Error deserializing Kafka message to OrderEventPayload: {}", messagePayload, e); // ИЗМЕНЕНО
        } catch (Exception e) {
            log.error("Error processing Kafka message: {}", messagePayload, e);
        }
    }
}