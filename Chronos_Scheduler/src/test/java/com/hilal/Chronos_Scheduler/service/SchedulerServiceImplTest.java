package com.hilal.Chronos_Scheduler.service;

import com.hilal.Chronos_Scheduler.entities.Job;
import com.hilal.Chronos_Scheduler.entities.JobExecution;
import com.hilal.Chronos_Scheduler.entities.enums.Status;
import com.hilal.Chronos_Scheduler.repository.JobExecutionRepository;
import com.hilal.Chronos_Scheduler.repository.JobRepository;
import com.hilal.Chronos_Scheduler.service.impl.SchedulerServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.*;

class SchedulerServiceImplTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private JobExecutionRepository jobExecutionRepository;

    @Mock
    private BlockingQueue<Job> jobQueue;

    @Mock
    private JobExecutionStateService jobExecutionStateService;

    @Mock
    private JobStateService jobStateService;

    @InjectMocks
    private SchedulerServiceImpl schedulerService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("resetStatusReservedJobs_service should reset stuck jobs to PENDING")
    void resetStatusReservedJobs_serviceResetsStuckJobsToPending() {
        Job job = new Job();
        job.setStatus(Status.RESERVED);
        List<Job> stuckJobs = List.of(job);

        when(jobRepository.lockStuckJobs()).thenReturn(stuckJobs);

        schedulerService.resetStatusReservedJobs_service();

        verify(jobRepository).saveAll(stuckJobs);
        Assertions.assertEquals(job.getStatus(), Status.PENDING);
    }

    @Test
    @DisplayName("getJobsToExecute_service should add jobs to queue and set status to RUNNING")
    void getJobsToExecute_serviceAddsJobsToQueueAndSetsStatusToRunning() throws InterruptedException {
        Job job = new Job();
        job.setStatus(Status.PENDING);
        List<Job> jobsToExecute = List.of(job);

        when(jobStateService.getJobsToExecuteBatch(10)).thenReturn(jobsToExecute);
        when(jobQueue.offer(job, 1000, TimeUnit.MILLISECONDS)).thenReturn(true);

        schedulerService.getJobsToExecute_service();

        verify(jobStateService).setJobAsRunning(job);
    }

    @Test
    @DisplayName("getCompletedJobExecutions_service should update recurring jobs and disable non-recurring jobs")
    void getCompletedJobExecutions_serviceUpdatesRecurringAndNonRecurringJobs() {
        Job recurringJob = new Job();
        recurringJob.setRecurring(true);
        recurringJob.setIntervalSeconds(3600);

        Job nonRecurringJob = new Job();
        nonRecurringJob.setRecurring(false);

        JobExecution recurringJobExecution = new JobExecution();
        recurringJobExecution.setJob(recurringJob);

        JobExecution nonRecurringJobExecution = new JobExecution();
        nonRecurringJobExecution.setJob(nonRecurringJob);

        List<JobExecution> completedJobs = List.of(recurringJobExecution, nonRecurringJobExecution);

        when(jobExecutionStateService.setCompletedJobExecutionAsPreserved(10)).thenReturn(completedJobs);

        schedulerService.getCompletedJobExecutions_service();


        Assertions.assertEquals(Status.PENDING, recurringJob.getStatus());
        Assertions.assertNotNull(recurringJob.getNextExecutionTime());
        Assertions.assertFalse(nonRecurringJob.isEnabled());
        Assertions.assertEquals(Status.COMPLETED, nonRecurringJob.getStatus());
    }

    @Test
    @DisplayName("getFailedJobExecutions_service should update job status based on recurrence")
    void getFailedJobExecutions_serviceUpdatesJobStatusBasedOnRecurrence() {
        Job recurringJob = new Job();
        recurringJob.setRecurring(true);
        recurringJob.setIntervalSeconds(3600);

        Job nonRecurringJob = new Job();
        nonRecurringJob.setRecurring(false);

        JobExecution recurringJobExecution = new JobExecution();
        recurringJobExecution.setJob(recurringJob);

        JobExecution nonRecurringJobExecution = new JobExecution();
        nonRecurringJobExecution.setJob(nonRecurringJob);

        List<JobExecution> failedJobs = List.of(recurringJobExecution, nonRecurringJobExecution);

        when(jobExecutionStateService.setFailedJobExecutionAsPreserved(10)).thenReturn(failedJobs);

        schedulerService.getFailedJobExecutions_service();

        Assertions.assertEquals(Status.PENDING, recurringJob.getStatus());
        Assertions.assertNotNull(recurringJob.getNextExecutionTime());
        Assertions.assertFalse(nonRecurringJob.isEnabled());
        Assertions.assertEquals(Status.FAILED, nonRecurringJob.getStatus());
    }

    @Test
    @DisplayName("getTimedOutJobExecutions_service should handle retries and disable jobs after max retries")
    void getTimedOutJobExecutions_serviceHandlesRetriesAndDisablesJobsAfterMaxRetries() {
        Job jobWithRetries = new Job();
        jobWithRetries.setRetryCount(1);
        jobWithRetries.setMaxRetry(3);
        jobWithRetries.setIntervalSeconds(3600);

        Job jobWithoutRetries = new Job();
        jobWithoutRetries.setRetryCount(3);
        jobWithoutRetries.setMaxRetry(3);

        JobExecution jobExecutionWithRetries = new JobExecution();
        jobExecutionWithRetries.setJob(jobWithRetries);

        JobExecution jobExecutionWithoutRetries = new JobExecution();
        jobExecutionWithoutRetries.setJob(jobWithoutRetries);

        List<JobExecution> timedOutJobs = List.of(jobExecutionWithRetries, jobExecutionWithoutRetries);

        when(jobExecutionStateService.setTimedOutJobExecutionAsPreserved(10)).thenReturn(timedOutJobs);

        schedulerService.getTimedOutJobExecutions_service();

        Assertions.assertEquals(jobWithRetries.getStatus(), Status.PENDING);
        Assertions.assertNotNull(jobWithRetries.getNextExecutionTime());
        Assertions.assertEquals(jobWithRetries.getRetryCount(), 2);
        Assertions.assertFalse(jobWithoutRetries.isEnabled());
        Assertions.assertEquals(jobWithoutRetries.getStatus(), Status.FAILED);
    }
    @Test
    @DisplayName("resetStatusReservedJobs_service should handle empty list without errors")
    void resetStatusReservedJobs_serviceHandlesEmptyList() {
        when(jobRepository.lockStuckJobs()).thenReturn(Collections.emptyList());

        schedulerService.resetStatusReservedJobs_service();

        verify(jobRepository,never()).saveAll(anyList());
    }

    @Test
    @DisplayName("getJobsToExecute_service should set job to PENDING when queue offer returns false")
    void getJobsToExecute_serviceSetsPendingWhenOfferFails() throws InterruptedException {
        Job job = new Job();
        List<Job> jobsToExecute = List.of(job);

        when(jobStateService.getJobsToExecuteBatch(10)).thenReturn(jobsToExecute);
        when(jobQueue.offer(job, 1000, TimeUnit.MILLISECONDS)).thenReturn(false);

        schedulerService.getJobsToExecute_service();

        verify(jobStateService).setJobAsPending(job);
        verify(jobStateService, never()).setJobAsRunning(job);
    }
    @Test
    @DisplayName("getJobsToExecute_service should set job to PENDING when offer throws InterruptedException")
    void getJobsToExecute_serviceHandlesInterruptedExceptionFromOffer() throws InterruptedException {
        Job job = new Job();
        List<Job> jobsToExecute = List.of(job);

        when(jobStateService.getJobsToExecuteBatch(10)).thenReturn(jobsToExecute);
        when(jobQueue.offer(job, 1000, TimeUnit.MILLISECONDS)).thenThrow(new InterruptedException());

        schedulerService.getJobsToExecute_service();

        verify(jobStateService).setJobAsPending(job);
    }

    @Test
    @DisplayName("getCompletedJobExecutions_service should handle empty list without errors")
    void getCompletedJobExecutions_serviceHandlesEmptyList() {
        when(jobExecutionStateService.setCompletedJobExecutionAsPreserved(10)).thenReturn(Collections.emptyList());

        schedulerService.getCompletedJobExecutions_service();

        verify(jobRepository,never()).saveAll(anyList());
    }

    @Test
    @DisplayName("getCompletedJobExecutions_service should throw NullPointerException when JobExecution has null job")
    void getCompletedJobExecutions_serviceThrowsWhenJobIsNull() {
        JobExecution je = new JobExecution();
        je.setJob(null);

        when(jobExecutionStateService.setCompletedJobExecutionAsPreserved(10)).thenReturn(List.of(je));

        Assertions.assertThrows(NullPointerException.class, () -> schedulerService.getCompletedJobExecutions_service());
    }

    @Test
    @DisplayName("getFailedJobExecutions_service should handle empty list without errors")
    void getFailedJobExecutions_serviceHandlesEmptyList() {
        when(jobExecutionStateService.setFailedJobExecutionAsPreserved(10)).thenReturn(Collections.emptyList());

        schedulerService.getFailedJobExecutions_service();

        verify(jobRepository,never()).saveAll(anyList());
    }

    @Test
    @DisplayName("getFailedJobExecutions_service should throw NullPointerException when JobExecution has null job")
    void getFailedJobExecutions_serviceThrowsWhenJobIsNull() {
        JobExecution je = new JobExecution();
        je.setJob(null);

        when(jobExecutionStateService.setFailedJobExecutionAsPreserved(10)).thenReturn(List.of(je));

        Assertions.assertThrows(NullPointerException.class, () -> schedulerService.getFailedJobExecutions_service());
    }

    @Test
    @DisplayName("getTimedOutJobExecutions_service should handle empty list without errors")
    void getTimedOutJobExecutions_serviceHandlesEmptyList() {
        when(jobExecutionStateService.setTimedOutJobExecutionAsPreserved(10)).thenReturn(Collections.emptyList());

        schedulerService.getTimedOutJobExecutions_service();

        verify(jobRepository,never()).saveAll(anyList());
    }

    @Test
    @DisplayName("getTimedOutJobExecutions_service should throw NullPointerException when JobExecution has null job")
    void getTimedOutJobExecutions_serviceThrowsWhenJobIsNull() {
        JobExecution je = new JobExecution();
        je.setJob(null);

        when(jobExecutionStateService.setTimedOutJobExecutionAsPreserved(10)).thenReturn(List.of(je));

       Assertions.assertThrows(NullPointerException.class, () -> schedulerService.getTimedOutJobExecutions_service());
    }

    @Test
    @DisplayName("getTimedOutJobExecutions_service should disable job immediately when maxRetry is zero")
    void getTimedOutJobExecutions_serviceDisablesJobWhenMaxRetryIsZero() {
        Job job = new Job();
        job.setRetryCount(0);
        job.setMaxRetry(0);
        job.setEnabled(true);

        JobExecution je = new JobExecution();
        je.setJob(job);

        when(jobExecutionStateService.setTimedOutJobExecutionAsPreserved(10)).thenReturn(List.of(je));

        schedulerService.getTimedOutJobExecutions_service();

        Assertions.assertFalse(job.isEnabled());
        Assertions.assertEquals(job.getStatus(), Status.FAILED);
        verify(jobRepository).saveAll(argThat(iter ->
                ((List<Job>) iter).contains(job)
        ));
    }
}
