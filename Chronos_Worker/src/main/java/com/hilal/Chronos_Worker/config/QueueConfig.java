package com.hilal.Chronos_Worker.config;

import com.hilal.Chronos_Worker.entities.JobExecution;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Configuration
public class QueueConfig {
    @Bean(name = "workerQueue")
    public BlockingQueue<JobExecution> workerQueue() {
        return new LinkedBlockingQueue<>(100);
    }
}
