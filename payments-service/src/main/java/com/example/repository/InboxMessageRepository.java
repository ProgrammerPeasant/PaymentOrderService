package com.example.repository;

import com.example.model.InboxMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface InboxMessageRepository extends JpaRepository<InboxMessage, String> {
    Optional<InboxMessage> findByMessageIdAndType(String messageId, String type);
    boolean existsByMessageIdAndTypeAndProcessedAtIsNotNull(String messageId, String type);
}