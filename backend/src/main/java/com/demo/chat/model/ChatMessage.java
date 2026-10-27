package com.demo.chat.model;

import java.time.Instant;

public class ChatMessage {

    private String id;
    private String sender;
    private String content;
    private String channel;
    private Instant timestamp;
    private String threadName;

    public ChatMessage() {
    }

    public ChatMessage(String id, String sender, String content, String channel, Instant timestamp, String threadName) {
        this.id = id;
        this.sender = sender;
        this.content = content;
        this.channel = channel;
        this.timestamp = timestamp;
        this.threadName = threadName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getThreadName() {
        return threadName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }
}
