package com.hilal.Chronos_Worker.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.BlockingQueue;

@Configuration
public class MetricsConfig {

    @Bean
    public Counter jobsCompleted(MeterRegistry registry) {
        return Counter.builder("chronos_worker_jobs_completed_total")
                .description("Total successful job executions")
                .register(registry);
    }

    @Bean
    public Counter jobsFailed(MeterRegistry registry) {
        return Counter.builder("chronos_worker_jobs_failed_total")
                .description("Total failed job executions")
                .register(registry);
    }

    @Bean
    public Counter workerJobDispatches(MeterRegistry registry) {
        return Counter.builder("chronos_worker_dispatch_total")
                .description("Total jobs dispatched from queue")
                .register(registry);
    }

    @Bean
    public Gauge workerQueueDepth(BlockingQueue<?> workerQueue, MeterRegistry registry) {
        return Gauge.builder("chronos_worker_queue_depth", workerQueue::size)
                .description("Number of jobs waiting in the worker queue")
                .register(registry);
    }
}
