package com.example.config;

import com.example.handler.OrderStatusUpdateHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final OrderStatusUpdateHandler orderStatusUpdateHandler;

    public WebSocketConfig(OrderStatusUpdateHandler orderStatusUpdateHandler) {
        this.orderStatusUpdateHandler = orderStatusUpdateHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(orderStatusUpdateHandler, "/ws/order-status")
                .setAllowedOrigins("*"); // Для разработки разрешаем все источники, в проде настройте конкретнее
    }
}