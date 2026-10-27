package com.demo.chat.controller;

import com.demo.chat.model.ThreadSnapshot;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

@RestController
@RequestMapping("/api/threads")
public class ThreadMonitorController {

    private final ThreadPoolExecutor socketWorkerPool;

    public ThreadMonitorController(ThreadPoolExecutor socketWorkerPool) {
        this.socketWorkerPool = socketWorkerPool;
    }

    @GetMapping
    public Map<String, Object> threads() {
        List<ThreadSnapshot> snapshots = new ArrayList<>();
        Thread.getAllStackTraces().keySet().stream()
                .filter(thread -> {
                    String name = thread.getName();
                    return name.startsWith("tcp-")
                            || name.startsWith("socket-worker-")
                            || name.startsWith("http-nio-")
                            || name.contains("websocket");
                })
                .sorted(Comparator.comparing(Thread::getName))
                .forEach(thread -> snapshots.add(new ThreadSnapshot(
                        thread.getName(),
                        thread.getState().name(),
                        thread.isAlive(),
                        thread.isDaemon()
                )));

        Map<String, Object> pool = new HashMap<>();
        pool.put("corePoolSize", socketWorkerPool.getCorePoolSize());
        pool.put("maximumPoolSize", socketWorkerPool.getMaximumPoolSize());
        pool.put("activeCount", socketWorkerPool.getActiveCount());
        pool.put("poolSize", socketWorkerPool.getPoolSize());
        pool.put("queueSize", socketWorkerPool.getQueue().size());
        pool.put("completedTaskCount", socketWorkerPool.getCompletedTaskCount());

        Map<String, Object> result = new HashMap<>();
        result.put("pool", pool);
        result.put("threads", snapshots);
        return result;
    }
}
