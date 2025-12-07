package com.hilal.Chronos_Scheduler.config;

import com.hilal.Chronos_Scheduler.entities.Job;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Configuration
public class QueueConfig {

    @Value("${scheduler.queue.capacity:100}")
    private int queueCapacity;

    @Bean(name = "jobQueue")
    public BlockingQueue<Job> jobQueue() {
        return new LinkedBlockingQueue<>(queueCapacity);
    }
}
