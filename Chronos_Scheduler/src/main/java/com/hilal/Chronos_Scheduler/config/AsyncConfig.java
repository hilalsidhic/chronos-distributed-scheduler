package com.hilal.Chronos_Scheduler.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Value("${scheduler.executor.pool_size:5}")
    private int corePoolSize;

    @Value("${scheduler.executor.max_pool_size:10}")
    private int maxPoolSize;

    @Value("${scheduler.executor.queue_capacity:25}")
    private int queueCapacity;

    @Bean(name = "jobExecutor")
    public ThreadPoolTaskExecutor jobExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setThreadNamePrefix("JobExecutor-");
        executor.setQueueCapacity(25);
        executor.initialize();
        return executor;
    }
}
