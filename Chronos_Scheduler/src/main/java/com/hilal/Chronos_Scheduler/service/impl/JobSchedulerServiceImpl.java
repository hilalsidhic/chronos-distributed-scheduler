package com.hilal.Chronos_Scheduler.service.impl;

import com.hilal.Chronos_Scheduler.config.SchedulerProperties;
import com.hilal.Chronos_Scheduler.entities.Job;
import com.hilal.Chronos_Scheduler.repository.JobRepository;
import com.hilal.Chronos_Scheduler.service.JobSchedulerService;
import io.micrometer.core.instrument.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class JobSchedulerServiceImpl implements JobSchedulerService {

    private final JobRepository jobRepository;
    private final SchedulerProperties properties;

    // --- METRICS ---
    private final Counter triggeredCounter;
    private final Counter failureCounter;
    private final Counter rowsCreatedCounter;

    private final Timer dbQueryTimer;
    private final DistributionSummary lagSummary;
    private final DistributionSummary fetchSizeSummary;

    private final AtomicInteger pendingJobsGauge = new AtomicInteger(0);

    public JobSchedulerServiceImpl(JobRepository jobRepository,
                               MeterRegistry registry,
                               SchedulerProperties properties) {

        this.jobRepository = jobRepository;
        this.properties = properties;

        // COUNTERS
        triggeredCounter = registry.counter("chronos_scheduler_jobs_triggered_total");
        failureCounter = registry.counter("chronos_scheduler_failures_total");
        rowsCreatedCounter = registry.counter("chronos_scheduler_job_execution_rows_created_total");

        fetchSizeSummary = DistributionSummary.builder("chronos_scheduler_fetch_size")
                .description("Number of jobs fetched in a batch")
                .register(registry);


        // 1. FIX DB TIMER (Add Percentile Histogram)
        dbQueryTimer = Timer.builder("chronos_scheduler_db_query_time")
                .description("How long DB fetch takes")
                .publishPercentileHistogram() // <--- CRITICAL LINE: Generates _bucket
                .register(registry);

// 2. FIX LAG SUMMARY (Add Percentile Histogram)
        lagSummary = DistributionSummary.builder("chronos_scheduler_job_lag_seconds")
                .description("How late jobs are being executed")
                .baseUnit("seconds")
                .publishPercentileHistogram() // <--- CRITICAL LINE: Generates _bucket
                .register(registry);

        // GAUGE
        Gauge.builder("chronos_scheduler_pending_jobs_current", pendingJobsGauge, AtomicInteger::get)
                .description("Current number of ready jobs")
                .register(registry);
    }

    @Override
    @Scheduled(fixedDelayString = "${chronos.poller.delay-ms:100}")
    @Transactional
    public void executionCycle() {
        int batchSize = properties.getMax().getJobsProcess();

        // TIME DB FETCH
        Timer.Sample sample = Timer.start();
        List<Job> jobs = jobRepository.lockPendingJobs(batchSize);
        sample.stop(dbQueryTimer);

        // UPDATE GAUGE + FETCH SIZE DISTRIBUTION
        pendingJobsGauge.set(jobs.size());
        fetchSizeSummary.record(jobs.size());

        if (jobs.isEmpty()) return;

        long nowMs = Instant.now().toEpochMilli();

        for (Job job : jobs) {
            try {
                // --- LAG METRIC ---
                if (job.getNextExecutionTime() != null) {
                    long scheduledMs = job.getNextExecutionTime().toInstant().toEpochMilli();
                    long lagMs = nowMs - scheduledMs;
                    lagSummary.record(lagMs / 1000.0);
                }

                // === YOUR REAL EXECUTION CREATION LOGIC HERE ===
                // createExecution(job);   <-- your function
                // update job.nextExecutionTime(...)
                // repo.save()

                triggeredCounter.increment();
                rowsCreatedCounter.increment();

            } catch (Exception e) {
                failureCounter.increment();
                throw e;
            }
        }
    }
}
