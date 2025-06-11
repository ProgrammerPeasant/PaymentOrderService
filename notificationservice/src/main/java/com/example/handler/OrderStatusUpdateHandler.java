package com.example.handler;

import com.example.dto.OrderEventPayload;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
// ... другие импорты ...
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;


@Component
@Slf4j
@RequiredArgsConstructor
public class OrderStatusUpdateHandler extends TextWebSocketHandler {
    private final Map<String, List<WebSocketSession>> userSessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;


    public void sendOrderStatusUpdate(String userId, OrderEventPayload eventPayload) {
        List<WebSocketSession> sessions = userSessions.get(userId);
        if (sessions != null && !sessions.isEmpty()) {
            try {
                Map<String, Object> messageForFrontend = new HashMap<>();
                messageForFrontend.put("id", eventPayload.orderId());
                messageForFrontend.put("userId", eventPayload.userId());
                messageForFrontend.put("totalAmount", eventPayload.amount());
                messageForFrontend.put("status", "CREATED");

                String messageJson = objectMapper.writeValueAsString(messageForFrontend);
                TextMessage message = new TextMessage(messageJson);

                log.info("Sending update for order {} to userId {}: {}", eventPayload.orderId(), userId, messageJson);
                for (WebSocketSession session : sessions) {
                    if (session.isOpen()) {
                        try {
                            session.sendMessage(message);
                        } catch (IOException e) {
                            log.error("Failed to send message to session {} for userId {}: {}", session.getId(), userId, e.getMessage());
                        }
                    }
                }
            } catch (IOException e) {
                log.error("Error serializing message for frontend for userId {}: {}", userId, e.getMessage(), e);
            }
        } else {
            log.warn("No active WebSocket sessions found for userId: {} to send order update.", userId);
        }
    }
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String query = session.getUri().getQuery();
        String userId = extractUserIdFromQuery(query);

        if (userId == null || userId.trim().isEmpty()) {
            log.warn("Connection established without userId in query, session id: {}. Closing.", session.getId());
            session.close(CloseStatus.BAD_DATA.withReason("userId query parameter is required"));
            return;
        }

        log.info("WebSocket connection established for userId: {}, session id: {}", userId, session.getId());
        userSessions.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(session);
    }

    private String extractUserIdFromQuery(String query) {
        if (query == null) return null;
        for (String param : query.split("&")) {
            String[] pair = param.split("=");
            if (pair.length == 2 && "userId".equals(pair[0])) {
                return pair[1];
            }
        }
        return null;
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        log.info("Received message from session {}: {}", session.getId(), message.getPayload());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String query = session.getUri().getQuery();
        String userId = extractUserIdFromQuery(query);

        if (userId != null) {
            List<WebSocketSession> sessions = userSessions.get(userId);
            if (sessions != null) {
                sessions.remove(session);
                if (sessions.isEmpty()) {
                    userSessions.remove(userId);
                }
            }
            log.info("WebSocket connection closed for userId: {}, session id: {}. Status: {}", userId, session.getId(), status);
        } else {
            log.info("WebSocket connection closed for session id: {}. Status: {}", session.getId(), status);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("Transport error for session {}: {}", session.getId(), exception.getMessage());
        String query = session.getUri().getQuery();
        String userId = extractUserIdFromQuery(query);
        if (userId != null) {
            List<WebSocketSession> sessions = userSessions.get(userId);
            if (sessions != null) {
                sessions.remove(session);
                if (sessions.isEmpty()) {
                    userSessions.remove(userId);
                }
            }
        }
    }
}