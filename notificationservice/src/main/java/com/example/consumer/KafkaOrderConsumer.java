package com.example.consumer;

import com.example.dto.OrderNotificationPayload;
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

    @KafkaListener(topics = "${kafka.topic.orderStatusUpdates:order-status-updates}",
            groupId = "${kafka.consumer.groupId:notification-group}")
    public void listenOrderUpdates(String messagePayload) {
        log.info("Received message from Kafka: {}", messagePayload);
        try {
            OrderNotificationPayload notificationPayload = objectMapper.readValue(messagePayload, OrderNotificationPayload.class); // Десериализуем в новый DTO
            log.info("Deserialized to OrderNotificationPayload: {}", notificationPayload);

            if (notificationPayload.userId() == null || notificationPayload.userId().trim().isEmpty()) {
                log.warn("Received order update without userId, cannot send WebSocket notification. Payload: {}", messagePayload);
                return;
            }

            orderStatusUpdateHandler.sendOrderStatusUpdate(notificationPayload.userId(), notificationPayload);

        } catch (JsonProcessingException e) {
            log.error("Error deserializing Kafka message to OrderNotificationPayload: {}", messagePayload, e);
        } catch (Exception e) {
            log.error("Error processing Kafka message: {}", messagePayload, e);
        }
    }
}