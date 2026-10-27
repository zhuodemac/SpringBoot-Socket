package com.demo.chat.socket;

import com.demo.chat.service.ChatHub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class SocketClientHandler implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(SocketClientHandler.class);

    private final String clientId;
    private final Socket socket;
    private final ChatHub chatHub;

    public SocketClientHandler(String clientId, Socket socket, ChatHub chatHub) {
        this.clientId = clientId;
        this.socket = socket;
        this.chatHub = chatHub;
    }

    @Override
    public void run() {
        try (
                Socket ignored = socket;
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true)
        ) {
            chatHub.registerTcpClient(clientId, writer);
            writer.println("WELCOME " + clientId + " thread=" + Thread.currentThread().getName());
            chatHub.publish("system", clientId + " joined via TCP Socket", "tcp");

            String line;
            while ((line = reader.readLine()) != null) {
                String content = line.trim();
                if (content.isEmpty()) {
                    continue;
                }
                if ("quit".equalsIgnoreCase(content) || "exit".equalsIgnoreCase(content)) {
                    writer.println("BYE");
                    break;
                }
                chatHub.publish(clientId, content, "tcp");
            }
        } catch (IOException e) {
            log.info("TCP client {} disconnected: {}", clientId, e.getMessage());
        } finally {
            chatHub.unregisterTcpClient(clientId);
            chatHub.publish("system", clientId + " left TCP Socket", "tcp");
        }
    }
}
