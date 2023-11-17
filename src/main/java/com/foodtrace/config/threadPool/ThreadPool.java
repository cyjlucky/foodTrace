package com.foodtrace.config.threadPool;

import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;


/**
 * 线程池配置类
 */
public class ThreadPool {
    @Bean
    public Executor threadPool() {
        // 创建一个线程池任务执行器
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 设置核心线程数为10
        executor.setCorePoolSize(10);
        // 设置最大线程数为20
        executor.setMaxPoolSize(20);
        // 设置队列容量为200
        executor.setQueueCapacity(200);
        // 设置线程名前缀为"taskExecutor-"
        executor.setThreadNamePrefix("taskExecutor-");
        // 初始化线程池任务执行器
        executor.initialize();
        // 返回线程池任务执行器实例
        return executor;
    }

}