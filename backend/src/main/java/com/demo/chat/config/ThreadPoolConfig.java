package com.demo.chat.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
public class ThreadPoolConfig {

    /**
     *  创建一个线程池，用于处理socket请求
     * @param coreSize 核心线程数
     * @param maxSize 最大线程数
     * @param queueCapacity 队列容量
     * @param prefix 线程名称前缀
     * @return ThreadPoolExecutor  线程池对象
     */
    @Bean(destroyMethod = "shutdown")
    public ThreadPoolExecutor socketWorkerPool(
            @Value("${app.thread-pool.core-size}") int coreSize,// 核心线程数
            @Value("${app.thread-pool.max-size}") int maxSize, // 最大线程数
            @Value("${app.thread-pool.queue-capacity}") int queueCapacity, // 队列容量
            @Value("${app.thread-pool.thread-name-prefix}") String prefix // 线程名称前缀
    ) {
        // 创建一个线程工厂，用于给线程池中的线程命名
        AtomicInteger index = new AtomicInteger(1);
        ThreadFactory factory = runnable -> {
            Thread thread = new Thread(runnable);
            thread.setName(prefix + index.getAndIncrement());
            thread.setDaemon(true);
            return thread;
        };

        return new ThreadPoolExecutor(
                coreSize,
                maxSize,
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(queueCapacity),
                factory,
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }
}
