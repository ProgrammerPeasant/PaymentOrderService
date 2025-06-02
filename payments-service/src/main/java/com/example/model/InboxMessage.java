package com.example.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments_inbox_messages")
@Getter @Setter @NoArgsConstructor
public class InboxMessage {
    @Id
    private String messageId; // orderId

    private String type; // OrderCreatedEvent

    @Column(columnDefinition = "TEXT")
    private String payload;

    private LocalDateTime receivedAt;
    private LocalDateTime processedAt;

    public InboxMessage(String messageId, String type, String payload) {
        this.messageId = messageId;
        this.type = type;
        this.payload = payload;
        this.receivedAt = LocalDateTime.now();
    }
}