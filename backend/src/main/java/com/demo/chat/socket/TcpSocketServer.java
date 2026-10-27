package com.demo.chat.socket;

import com.demo.chat.service.ChatHub;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class TcpSocketServer {

    private static final Logger log = LoggerFactory.getLogger(TcpSocketServer.class);

    private final ChatHub chatHub;
    private final ThreadPoolExecutor socketWorkerPool;
    private final int port;
    private final int backlog;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicInteger clientSequence = new AtomicInteger(1);

    private ServerSocket serverSocket;
    private Thread acceptorThread;

    public TcpSocketServer(
            ChatHub chatHub,
            ThreadPoolExecutor socketWorkerPool,
            @Value("${app.tcp.port}") int port,
            @Value("${app.tcp.backlog}") int backlog
    ) {
        this.chatHub = chatHub;
        this.socketWorkerPool = socketWorkerPool;
        this.port = port;
        this.backlog = backlog;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void start() {
        if (!running.compareAndSet(false, true)) {
            return;
        }
        try {
            serverSocket = new ServerSocket();
            serverSocket.setReuseAddress(true);
            serverSocket.bind(new InetSocketAddress("0.0.0.0", port), backlog);
            acceptorThread = new Thread(this::acceptLoop, "tcp-acceptor");
            acceptorThread.setDaemon(true);
            acceptorThread.start();
            log.info("TCP Socket server listening on port {}", port);
        } catch (IOException e) {
            running.set(false);
            throw new IllegalStateException("Unable to start TCP socket server on port " + port, e);
        }
    }

    private void acceptLoop() {
        while (running.get() && serverSocket != null && !serverSocket.isClosed()) {
            try {
                Socket client = serverSocket.accept();
                String clientId = "tcp-" + clientSequence.getAndIncrement();
                log.info("Accepted TCP client {} from {}", clientId, client.getRemoteSocketAddress());
                socketWorkerPool.execute(new SocketClientHandler(clientId, client, chatHub));
            } catch (SocketException e) {
                if (running.get()) {
                    log.warn("TCP accept loop interrupted: {}", e.getMessage());
                }
            } catch (IOException e) {
                if (running.get()) {
                    log.error("Failed to accept TCP client", e);
                }
            }
        }
    }

    @PreDestroy
    public void stop() {
        running.set(false);
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                log.warn("Error closing TCP server socket", e);
            }
        }
        if (acceptorThread != null) {
            acceptorThread.interrupt();
        }
        log.info("TCP Socket server stopped");
    }
}
