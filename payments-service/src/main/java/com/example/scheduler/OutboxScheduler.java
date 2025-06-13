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
                                log.info("Message {} published successfully to topic {}", message.getId(), message.getTopic());
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