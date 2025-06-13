package com.example.scheduler;

import com.example.model.OutboxMessage;
import com.example.repository.OutboxMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxScheduler {
    private final OutboxMessageRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedDelay = 500)
    @Transactional
    public void pollAndPublish() {
        List<OutboxMessage> messages = outboxRepository.findByProcessedAtIsNullOrderByCreatedAtAsc();
        if (messages.isEmpty()) {
            return;
        }
        log.info("Found {} messages in outbox to publish.", messages.size());

        for (OutboxMessage message : messages) {
            try {
                kafkaTemplate.send(message.getTopic(), message.getMessageKey(), message.getPayload())
                        .whenComplete((result, ex) -> {
                            if (ex == null) {
                                // This part needs to be handled carefully.
                                // Updating DB here is outside the poller's transaction if send is async.
                                // For simplicity, we assume synchronous send or handle completion carefully.
                                // A better approach is a separate transaction for updating after confirm.
                                // For now, we'll update it directly if no exception.
                                // This is simplified; robust solutions might use a separate process/thread for Kafka callbacks
                                // or make the send synchronous within a new transaction per message.
                                log.info("Message {} published successfully to topic {}", message.getId(), message.getTopic());
                                // This update should be in its own transaction or use a method that starts one.
                                // However, if pollAndPublish is @Transactional, this update is part of it.
                                // If Kafka send is truly async, this is optimistic.
                                // For true reliability, Kafka send should be blocking or completion stage handles DB update.
                                // For this scope, we'll mark as processed immediately after a non-failing send call.
                                // A more robust implementation might re-fetch and update in a new transaction
                                // after the sendFuture completes successfully.
                            } else {
                                log.error("Failed to send message {} to Kafka topic {}: {}",
                                        message.getId(), message.getTopic(), ex.getMessage());
                            }
                        }).get();

                message.setProcessedAt(LocalDateTime.now());
                outboxRepository.save(message);

            } catch (Exception e) {
                log.error("Error publishing message {} from outbox: {}", message.getId(), e.getMessage(), e);
                break;
            }
        }
    }
}