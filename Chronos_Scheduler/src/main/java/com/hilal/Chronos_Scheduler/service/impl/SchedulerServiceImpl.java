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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

@Service
public class SchedulerServiceImpl implements SchedulerService {
    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private BlockingQueue<Job> jobQueue;

    @Autowired
    private JobExecutionStateService jobExecutionStateService;

    @Autowired
    private JobStateService jobStateService;

    @Override
    @Scheduled(fixedRate = 60000)
    public void resetStatusReservedJobs_service() {
        List<Job> failedJobs = jobRepository.lockStuckJobs();
        for(Job job : failedJobs) {
            job.setStatus(Status.PENDING);
        }
        jobRepository.saveAll(failedJobs);
        return;
    }

    @Override
    @Scheduled(fixedRate = 2000)
    public void getJobsToExecute_service() {
        List<Job> jobsToExecute = jobStateService.getJobsToExecuteBatch(10);
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
    public void getCompletedJobExecutions_service() {
        List<JobExecution> completedJobs = jobExecutionStateService.setCompletedJobExecutionAsPreserved(10);
        for(JobExecution jobExecution : completedJobs) {
            Job job = jobExecution.getJob();
            if (job.isRecurring()) {
                job.setNextExecutionTime(OffsetDateTime.now().plusSeconds(job.getIntervalSeconds()));
                job.setStatus(Status.PENDING);
            } else {
                job.setEnabled(false);
                job.setStatus(Status.COMPLETED);
            }
        }
        jobRepository.saveAll(
                completedJobs.stream()
                        .map(JobExecution::getJob)
                        .toList()
        );
        return;
    }

    @Override
    @Scheduled(fixedRate = 10000)
    public void getFailedJobExecutions_service() {
        List<JobExecution> failedJobs = jobExecutionStateService.setFailedJobExecutionAsPreserved(10);
        for(JobExecution jobExecution : failedJobs) {
            Job job = jobExecution.getJob();
            if (job.isRecurring()) {
                job.setNextExecutionTime(OffsetDateTime.now().plusSeconds(job.getIntervalSeconds()));
                job.setStatus(Status.PENDING);
            } else {
                job.setEnabled(false);
                job.setStatus(Status.FAILED);
            }
        }
        jobRepository.saveAll(
                failedJobs.stream()
                        .map(JobExecution::getJob)
                        .toList()
        );
        return;
    }


    @Override
    @Scheduled(fixedRate = 15000)
    public void getTimedOutJobExecutions_service() {
        List<JobExecution> timedOutJobs = jobExecutionStateService.setTimedOutJobExecutionAsPreserved(10);
        for(JobExecution jobExecution : timedOutJobs) {
            Job job = jobExecution.getJob();
            if(job.getRetryCount() >= job.getMaxRetry()) {
                job.setEnabled(false);
                job.setStatus(Status.FAILED);
                continue;
            }
            else{
                long backoffSeconds = job.getIntervalSeconds() * (1L << (job.getRetryCount())); // exponential
                job.setNextExecutionTime(OffsetDateTime.now().plusSeconds(backoffSeconds));
                job.setRetryCount(job.getRetryCount() + 1);
                job.setStatus(Status.PENDING);
            }
        }
        jobRepository.saveAll(
                timedOutJobs.stream()
                        .map(JobExecution::getJob)
                        .toList()
        );
        return;
    }
}
