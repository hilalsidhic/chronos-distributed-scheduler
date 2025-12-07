package com.hilal.Chronos_Scheduler.service.impl;

import com.hilal.Chronos_Scheduler.entities.Job;
import com.hilal.Chronos_Scheduler.entities.JobExecution;
import com.hilal.Chronos_Scheduler.entities.enums.ExecutionStatus;
import com.hilal.Chronos_Scheduler.entities.enums.Status;
import com.hilal.Chronos_Scheduler.repository.JobExecutionRepository;
import com.hilal.Chronos_Scheduler.repository.JobRepository;
import com.hilal.Chronos_Scheduler.service.JobExecutionStateService;
import com.hilal.Chronos_Scheduler.service.JobStateService;
import com.hilal.Chronos_Scheduler.service.SchedulerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

@Service
@ConditionalOnProperty(value = "scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class SchedulerServiceImpl implements SchedulerService {
    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private BlockingQueue<Job> jobQueue;

    @Autowired
    private JobExecutionStateService jobExecutionStateService;

    @Autowired
    private JobStateService jobStateService;

    @Value("${scheduler.max.jobs.process:10}")
    private int maxJobsToProcess;

    @Value("${scheduler.max.jobs.success:10}")
    private int maxSuccessfulJobsExecutionsToPreserve;

    @Value("${scheduler.max.jobs.failed:10}")
    private int maxFailedJobsExecutionsToPreserve;

    @Value("${scheduler.max.jobs.timedout:10}")
    private int maxTimedOutJobsExecutionsToPreserve;

    @Override
    @Scheduled(fixedRate = 60000)
    public void resetStatusReservedJobs_service() {
        List<Job> failedJobs = jobRepository.lockStuckJobs();
        if(failedJobs.isEmpty()) return;
        for(Job job : failedJobs) {
            job.setStatus(Status.PENDING);
        }
        jobRepository.saveAll(failedJobs);
        return;
    }

    @Override
    @Scheduled(fixedRate = 2000)
    public void getJobsToExecute_service() {
        List<Job> jobsToExecute = jobStateService.getJobsToExecuteBatch(maxJobsToProcess);
        if (jobsToExecute.isEmpty()) return;
        for (Job job : jobsToExecute) {
            try {
                boolean added = jobQueue.offer(job, 1000, TimeUnit.MILLISECONDS);
                if (added) jobStateService.setJobAsRunning(job);
                else jobStateService.setJobAsPending(job);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                jobStateService.setJobAsPending(job); // fallback
            }
        }
        return;
    }

    @Override
    @Scheduled(fixedRate = 5000)
    @Transactional
    public void getCompletedJobExecutions_service() {
        List<JobExecution> completedJobs = jobExecutionStateService.setCompletedJobExecutionAsPreserved(maxSuccessfulJobsExecutionsToPreserve);
        if (completedJobs.isEmpty()) return;
        for(JobExecution jobExecution : completedJobs) {
            Job job = jobExecution.getJob();
            if (job.isRecurring()) {
                job.setNextExecutionTime(OffsetDateTime.now().plusSeconds(job.getIntervalSeconds()));
                job.setStatus(Status.PENDING);
            } else {
                job.setEnabled(false);
                job.setStatus(Status.COMPLETED);
            }
            jobRepository.save(job);
        }
        return;
    }

    @Override
    @Scheduled(fixedRate = 10000)
    @Transactional
    public void getFailedJobExecutions_service() {
        List<JobExecution> failedJobs = jobExecutionStateService.setFailedJobExecutionAsPreserved(maxFailedJobsExecutionsToPreserve);
        if (failedJobs.isEmpty()) return;
        for(JobExecution jobExecution : failedJobs) {
            Job job = jobExecution.getJob();
            if (job.isRecurring()) {
                job.setNextExecutionTime(OffsetDateTime.now().plusSeconds(job.getIntervalSeconds()));
                job.setStatus(Status.PENDING);
            } else {
                job.setEnabled(false);
                job.setStatus(Status.FAILED);
            }
            jobRepository.save(job);
        }
        return;
    }

    @Override
    @Scheduled(fixedRate = 15000)
    @Transactional
    public void getTimedOutJobExecutions_service() {
        List<JobExecution> timedOutJobs = jobExecutionStateService.setTimedOutJobExecutionAsPreserved(maxTimedOutJobsExecutionsToPreserve);
         if (timedOutJobs.isEmpty()) return;
        for(JobExecution jobExecution : timedOutJobs) {
            Job job = jobExecution.getJob();
            if(job.getRetryCount() >= job.getMaxRetry() - 1) {
                job.setEnabled(false);
                job.setStatus(Status.FAILED);
                jobRepository.save(job);
                continue;
            }
            else{
                long backoffSeconds = 1L << (job.getRetryCount()); // exponential
                job.setNextExecutionTime(OffsetDateTime.now().plusSeconds(backoffSeconds));
                job.setRetryCount(job.getRetryCount() + 1);
                job.setStatus(Status.PENDING);
            }
            jobRepository.save(job);
        }
        return;
    }
}
