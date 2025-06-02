package com.example.service;

import com.example.event.OrderCreatedEvent;
import com.example.event.PaymentProcessedEvent;
import com.example.event.PaymentStatus;
import com.example.model.Account;
import com.example.model.InboxMessage;
import com.example.model.OutboxMessage;
import com.example.model.PaymentTransaction;
import com.example.repository.AccountRepository;
import com.example.repository.InboxMessageRepository;
import com.example.repository.OutboxMessageRepository;
import com.example.repository.PaymentTransactionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {
    private final AccountRepository accountRepository;
    private final OutboxMessageRepository outboxMessageRepository;
    private final InboxMessageRepository inboxMessageRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final ObjectMapper objectMapper;

    @Value("${app.kafka.payment-processed-topic}")
    private String paymentProcessedTopic;

    @Transactional
    public Account createAccount(String userId) {
        if (accountRepository.existsById(userId)) {
            throw new IllegalStateException("Account already exists for user: " + userId);
        }
        Account account = new Account(userId, BigDecimal.ZERO);
        return accountRepository.save(account);
    }

    @Transactional
    public Account depositToAccount(String userId, BigDecimal amount) {
        Account account = accountRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found for user: " + userId));

        int updatedRows = accountRepository.creditBalance(userId, amount);
        if (updatedRows == 0) {
            throw new RuntimeException("Failed to deposit to account for user: " + userId + ". " +
                    "Account might not exist" +
                    " or concurrency issue.");
        }
        return accountRepository.findById(userId).orElseThrow();
    }

    @Transactional(readOnly = true)
    public Account getAccountBalance(String userId) {
        return accountRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found for user: " + userId));
    }

    // кор логика
    @Transactional(propagation = Propagation.MANDATORY)
    public void processOrderPayment(OrderCreatedEvent event, String rawPayload) {
        String messageId = event.orderId();
        String messageType = OrderCreatedEvent.class.getSimpleName();

        // проверка на идемпотентность
        if (inboxMessageRepository.existsByMessageIdAndTypeAndProcessedAtIsNotNull(messageId, messageType)) {
            log.info("Order payment {} already processed, skipping.", messageId);
            return;
        }
        InboxMessage inboxMessage = inboxMessageRepository.findByMessageIdAndType(messageId, messageType)
                .orElseGet(() -> inboxMessageRepository.save(new InboxMessage(messageId, messageType, rawPayload)));

        // тоже проверка идемп.
        if (paymentTransactionRepository.existsByOrderId(event.orderId())) {
            log.warn("Payment transaction for order {} already exists. Marking inbox message as processed.",
                    event.orderId());
            inboxMessage.setProcessedAt(LocalDateTime.now());
            inboxMessageRepository.save(inboxMessage);
            return;
        }

        PaymentStatus status;
        String reason = null;
        PaymentTransaction paymentTransaction;

        int debitSuccess = accountRepository.debitBalance(event.userId(), event.amount());

        if (debitSuccess > 0) {
            status = PaymentStatus.SUCCESS;
            log.info("Successfully debited {} from user {} for order {}",
                    event.amount(), event.userId(), event.orderId());
        } else {
            if (!accountRepository.existsById(event.userId())) {
                status = PaymentStatus.FAILURE_ACCOUNT_NOT_FOUND;
                reason = "Account not found for user " + event.userId();
            } else {
                status = PaymentStatus.FAILURE_INSUFFICIENT_FUNDS;
                reason = "Insufficient funds for user " + event.userId();
            }
            log.warn("Failed to debit {} from user {} for order {}. Reason: {}",
                    event.amount(), event.userId(), event.orderId(), reason);
        }

        paymentTransaction = new PaymentTransaction(
                event.orderId(),
                event.userId(),
                event.amount(),
                status,
                reason
        );
        paymentTransactionRepository.save(paymentTransaction);

        PaymentProcessedEvent paymentProcessedEvent = new PaymentProcessedEvent(
                event.orderId(),
                event.userId(),
                paymentTransaction.getId(),
                status,
                event.amount(),
                reason
        );

        try {
            String payloadJson = objectMapper.writeValueAsString(paymentProcessedEvent);
            OutboxMessage outboxMessage = new OutboxMessage(
                    "Payment",
                    paymentTransaction.getId(),
                    paymentProcessedTopic,
                    event.orderId(),
                    payloadJson
            );
            outboxMessageRepository.save(outboxMessage);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize PaymentProcessedEvent for order {}", event.orderId(), e);
            throw new RuntimeException("Serialization error for payment processed event", e);
        }

        inboxMessage.setProcessedAt(LocalDateTime.now());
        inboxMessageRepository.save(inboxMessage);

        log.info("Payment processing complete for order {}. Status: {}", event.orderId(), status);
    }
}