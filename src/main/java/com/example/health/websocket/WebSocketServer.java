package com.example.health.websocket;

import com.example.health.utils.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/ws/{token}")
@Component
@Slf4j
public class WebSocketServer {

    // Store active sessions by user ID
    private static final Map<Long, Session> sessionMap = new ConcurrentHashMap<>();
    
    // We need to inject JwtUtil. Note that @ServerEndpoint creates a new instance per connection,
    // so we need a static workaround to inject Spring beans.
    private static JwtUtil jwtUtil;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public void setJwtUtil(JwtUtil jwtUtil) {
        WebSocketServer.jwtUtil = jwtUtil;
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("token") String token) {
        try {
            Long userId = jwtUtil.getUserIdFromToken(token);
            if (userId == null || !jwtUtil.validateToken(token)) {
                log.warn("Invalid token for WebSocket connection.");
                session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "Invalid token"));
                return;
            }
            sessionMap.put(userId, session);
            log.info("User {} connected to WebSocket. Total active: {}", userId, sessionMap.size());
        } catch (Exception e) {
            log.error("Error during WebSocket connection", e);
            try {
                session.close();
            } catch (IOException ignored) {}
        }
    }

    @OnClose
    public void onClose(Session session, @PathParam("token") String token) {
        try {
            Long userId = jwtUtil.getUserIdFromToken(token);
            if (userId != null) {
                sessionMap.remove(userId);
                log.info("User {} disconnected from WebSocket. Total active: {}", userId, sessionMap.size());
            }
        } catch (Exception e) {
            log.error("Error during WebSocket disconnection", e);
        }
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        // We typically handle client-to-server WS messages here if needed (e.g., typing indicators).
        // For this app, most logic is handled via HTTP APIs and we use WS for server-to-client push.
        log.info("Received message: {}", message);
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("WebSocket error: ", error);
    }

    /**
     * Send a message to a specific user.
     */
    public static void sendMessageToUser(Long userId, Object message) {
        Session session = sessionMap.get(userId);
        if (session != null && session.isOpen()) {
            try {
                String jsonMessage = objectMapper.writeValueAsString(message);
                session.getAsyncRemote().sendText(jsonMessage);
            } catch (Exception e) {
                log.error("Failed to send WebSocket message to user " + userId, e);
            }
        }
    }

    /**
     * Broadcast a message to all connected users.
     */
    public static void broadcastMessage(Object message) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(message);
            sessionMap.values().forEach(session -> {
                if (session.isOpen()) {
                    session.getAsyncRemote().sendText(jsonMessage);
                }
            });
        } catch (Exception e) {
            log.error("Failed to broadcast WebSocket message", e);
        }
    }
}
