package com.demo.chat.controller;

import com.demo.chat.dto.SendMessageRequest;
import com.demo.chat.model.ChatMessage;
import com.demo.chat.service.ChatHub;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ChatController {

    private final ChatHub chatHub;

    public ChatController(ChatHub chatHub) {
        this.chatHub = chatHub;
    }

    @GetMapping("/messages")
    public List<ChatMessage> messages() {
        return chatHub.recentMessages();
    }

    @PostMapping("/messages")
    public ChatMessage send(@Valid @RequestBody SendMessageRequest request) {
        return chatHub.publish(request.getSender(), request.getContent(), "http");
    }

    @GetMapping("/status")
    public Map<String, Object> status() {
        return Map.of(
                "webSessions", chatHub.webSessionCount(),
                "tcpClients", chatHub.tcpClientCount(),
                "messageCount", chatHub.recentMessages().size()
        );
    }
}
