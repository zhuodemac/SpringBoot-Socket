package com.demo.chat.service;

import com.demo.chat.model.ChatMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class ChatHub {

    private static final Logger log = LoggerFactory.getLogger(ChatHub.class);
    private static final int HISTORY_LIMIT = 100;

    private final ObjectMapper objectMapper;
    private final List<ChatMessage> history = new CopyOnWriteArrayList<>();
    private final Map<String, WebSocketSession> webSessions = new ConcurrentHashMap<>();
    private final Map<String, PrintWriter> tcpClients = new ConcurrentHashMap<>();

    public ChatHub(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public ChatMessage publish(String sender, String content, String channel) {
        ChatMessage message = new ChatMessage(
                UUID.randomUUID().toString(),
                sender,
                content,
                channel,
                Instant.now(),
                Thread.currentThread().getName()
        );
        history.add(message);
        if (history.size() > HISTORY_LIMIT) {
            history.remove(0);
        }
        broadcast(message);
        return message;
    }

    public List<ChatMessage> recentMessages() {
        return new ArrayList<>(history);
    }

    public void registerWebSession(WebSocketSession session) {
        webSessions.put(session.getId(), session);
    }

    public void unregisterWebSession(String sessionId) {
        webSessions.remove(sessionId);
    }

    public void registerTcpClient(String clientId, PrintWriter writer) {
        tcpClients.put(clientId, writer);
    }

    public void unregisterTcpClient(String clientId) {
        tcpClients.remove(clientId);
    }

    public int webSessionCount() {
        return webSessions.size();
    }

    public int tcpClientCount() {
        return tcpClients.size();
    }

    private void broadcast(ChatMessage message) {
        String json;
        try {
            json = objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize chat message", e);
            return;
        }

        webSessions.values().forEach(session -> {
            if (!session.isOpen()) {
                return;
            }
            synchronized (session) {
                try {
                    session.sendMessage(new TextMessage(json));
                } catch (IOException e) {
                    log.warn("WebSocket send failed for session {}", session.getId());
                }
            }
        });

        tcpClients.forEach((id, writer) -> {
            writer.println(json);
            if (writer.checkError()) {
                log.warn("TCP send failed for client {}", id);
            }
        });
    }
}
