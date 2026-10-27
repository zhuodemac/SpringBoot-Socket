package com.demo.chat.dto;

import jakarta.validation.constraints.NotBlank;

public class SendMessageRequest {

    @NotBlank
    private String sender;

    @NotBlank
    private String content;

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
}
