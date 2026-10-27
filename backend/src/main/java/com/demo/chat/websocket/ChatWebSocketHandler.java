package com.demo.chat.websocket;

import com.demo.chat.service.ChatHub;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(ChatWebSocketHandler.class);

    private final ChatHub chatHub;
    private final ObjectMapper objectMapper;

    public ChatWebSocketHandler(ChatHub chatHub, ObjectMapper objectMapper) {
        this.chatHub = chatHub;
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        chatHub.registerWebSession(session);
        log.info("WebSocket connected: {}", session.getId());
        chatHub.publish("system", "browser-" + session.getId().substring(0, 8) + " joined via WebSocket", "ws");
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        JsonNode node = objectMapper.readTree(message.getPayload());
        String sender = node.path("sender").asText("anonymous");
        String content = node.path("content").asText("").trim();
        if (!content.isEmpty()) {
            chatHub.publish(sender, content, "ws");
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        chatHub.unregisterWebSession(session.getId());
        log.info("WebSocket closed: {} {}", session.getId(), status);
        chatHub.publish("system", "browser-" + session.getId().substring(0, 8) + " left WebSocket", "ws");
    }
}
