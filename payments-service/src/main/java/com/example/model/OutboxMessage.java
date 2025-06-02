package com.example.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payments_outbox_messages")
@Getter @Setter @NoArgsConstructor
public class OutboxMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    private String aggregateType;

    @Column(nullable = false)
    private String aggregateId;

    @Column(nullable = false)
    private String topic;

    @Column(nullable = false)
    private String messageKey; // ключ сообщ. кафки

    @Column(columnDefinition = "TEXT", nullable = false)
    private String payload;

    private LocalDateTime createdAt;
    private LocalDateTime processedAt;

    public OutboxMessage(String aggregateType, String aggregateId, String topic, String messageKey, String payload) {
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.topic = topic;
        this.messageKey = messageKey;
        this.payload = payload;
        this.createdAt = LocalDateTime.now();
    }
}