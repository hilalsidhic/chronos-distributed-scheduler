package com.hilal.Chronos_Worker.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

@Configuration
public class AsyncConfig {

    @Value("${worker.executor.pool_size:20}")
    private int corePoolSize;

    @Value("${worker.executor.max_pool_size:50}")
    private int maxPoolSize;

    @Value("${worker.executor.queue_capacity:100}")
    private int queueCapacity;

    @Value("${worker.executor.termination_seconds:20}")
    private int terminationSeconds;

    @Bean(name = "workerExecutor")
    public ThreadPoolTaskExecutor workerExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix("WorkerExec-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(terminationSeconds);
        executor.initialize();

        return executor;
    }
}
