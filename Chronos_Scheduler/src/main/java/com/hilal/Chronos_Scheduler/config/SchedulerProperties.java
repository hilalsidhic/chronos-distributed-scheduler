package com.hilal.Chronos_Scheduler.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "scheduler")
public class SchedulerProperties {

    // Maps to 'scheduler.max.jobs.process'
    private Max max = new Max();

    // Maps to 'scheduler.queue.capacity' (if you implement internal queueing later)
    private int queueCapacity;

    public Max getMax() { return max; }
    public void setMax(Max max) { this.max = max; }

    public int getQueueCapacity() { return queueCapacity; }
    public void setQueueCapacity(int queueCapacity) { this.queueCapacity = queueCapacity; }

    public static class Max {
        private int jobsProcess = 500; // Default 500

        public int getJobsProcess() { return jobsProcess; }
        public void setJobsProcess(int jobsProcess) { this.jobsProcess = jobsProcess; }
    }
}