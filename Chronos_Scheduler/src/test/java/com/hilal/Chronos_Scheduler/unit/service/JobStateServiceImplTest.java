package com.hilal.Chronos_Scheduler.unit.service;

import com.hilal.Chronos_Scheduler.entities.Job;
import com.hilal.Chronos_Scheduler.entities.enums.Status;
import com.hilal.Chronos_Scheduler.repository.JobRepository;
import com.hilal.Chronos_Scheduler.service.impl.JobStateServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JobStateServiceImplTest {

    @Mock
    private JobRepository jobRepository;

    @InjectMocks
    private JobStateServiceImpl jobStateService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("setJobAsRunning should update status to RUNNING and save job")
    void setJobAsRunning_updatesStatusAndSaves() {
        Job job = new Job();
        job.setStatus(Status.PENDING);

        jobStateService.setJobAsRunning(job);

        assertEquals(Status.RUNNING, job.getStatus());
        verify(jobRepository).save(eq(job));
    }

    @Test
    @DisplayName("setJobAsPending should update status to PENDING and save job")
    void setJobAsPending_updatesStatusAndSaves() {
        Job job = new Job();
        job.setStatus(Status.RESERVED);

        jobStateService.setJobAsPending(job);

        assertEquals(Status.PENDING, job.getStatus());
        verify(jobRepository).save(eq(job));
    }

    @Test
    @DisplayName("getJobsToExecuteBatch locks jobs, sets RESERVED state, and saves them")
    void getJobsToExecuteBatch_updatesStatusAndSaves() {
        Job job = new Job();
        job.setStatus(Status.PENDING);

        List<Job> jobs = List.of(job);
        when(jobRepository.lockPendingJobs(5)).thenReturn(jobs);

        List<Job> result = jobStateService.getJobsToExecuteBatch(5);

        assertEquals(1, result.size());
        assertEquals(Status.RESERVED, result.get(0).getStatus());
        assertNotNull(result.get(0).getReservedAt());

        verify(jobRepository).saveAll(jobs);
    }

    @Test
    @DisplayName("getJobsToExecuteBatch returns empty list when no pending jobs")
    void getJobsToExecuteBatch_emptyList() {
        when(jobRepository.lockPendingJobs(5)).thenReturn(Collections.emptyList());

        List<Job> result = jobStateService.getJobsToExecuteBatch(5);

        assertTrue(result.isEmpty());

        verify(jobRepository).lockPendingJobs(5);
        verify(jobRepository, never()).saveAll(anyList());
    }
}
