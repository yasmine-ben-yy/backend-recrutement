// com.example.recrutement.notification.websocket.NotificationWebSocketHandler.java
package com.example.recrutement.notification.websocket;

import com.example.recrutement.notification.dto.NotificationDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class NotificationWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<Long, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long userId = extractUserId(session);
        if (userId != null) {
            sessions.put(userId, session);
            log.info("🔌 WebSocket connecté - utilisateur: {}", userId);
        }
        super.afterConnectionEstablished(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long userId = extractUserId(session);
        if (userId != null) {
            sessions.remove(userId);
            log.info("🔌 WebSocket déconnecté - utilisateur: {}", userId);
        }
        super.afterConnectionClosed(session, status);
    }

    public void sendNotification(Long userId, NotificationDTO notification) {
        WebSocketSession session = sessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                String message = objectMapper.writeValueAsString(notification);
                session.sendMessage(new TextMessage(message));
                log.info("📨 Notification envoyée en temps réel à l'utilisateur: {}", userId);
            } catch (Exception e) {
                log.error("❌ Erreur envoi WebSocket: {}", e.getMessage());
            }
        }
    }

    private Long extractUserId(WebSocketSession session) {
        String query = session.getUri().getQuery();
        if (query != null && query.contains("userId=")) {
            String userIdStr = query.split("userId=")[1];
            try {
                return Long.parseLong(userIdStr);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}