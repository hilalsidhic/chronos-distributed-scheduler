package com.hilal.Chronos_Scheduler.unit.service;

import com.hilal.Chronos_Scheduler.entities.JobExecution;
import com.hilal.Chronos_Scheduler.entities.enums.ExecutionStatus;
import com.hilal.Chronos_Scheduler.repository.JobExecutionRepository;
import com.hilal.Chronos_Scheduler.service.impl.JobExecutionStateServiceImpl;
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

class JobExecutionStateServiceImplTest {

    @Mock
    private JobExecutionRepository jobExecutionRepository;

    @InjectMocks
    private JobExecutionStateServiceImpl jobExecutionStateService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // ===========================
    // COMPLETED JOBS
    // ===========================

    @Test
    @DisplayName("setCompletedJobExecutionAsPreserved should update status to PRESERVED and save all completed jobs")
    void setCompletedJobExecutionAsPreserved_updatesStatusAndSavesCompletedJobs() {
        JobExecution je = new JobExecution();
        je.setStatus(ExecutionStatus.SUCCESS);

        List<JobExecution> completedJobs = List.of(je);

        when(jobExecutionRepository.lockCompletedJobExecutions(5)).thenReturn(completedJobs);
        when(jobExecutionRepository.saveAll(completedJobs)).thenReturn(completedJobs);

        List<JobExecution> result = jobExecutionStateService.setCompletedJobExecutionAsPreserved(5);

        assertEquals(1, result.size());
        assertEquals(ExecutionStatus.PRESERVED, result.get(0).getStatus());

        verify(jobExecutionRepository).lockCompletedJobExecutions(5);
        verify(jobExecutionRepository).saveAll(completedJobs);
    }

    @Test
    @DisplayName("setCompletedJobExecutionAsPreserved should return empty list when none are found")
    void setCompletedJobExecutionAsPreserved_emptyList() {
        when(jobExecutionRepository.lockCompletedJobExecutions(5)).thenReturn(Collections.emptyList());

        List<JobExecution> result = jobExecutionStateService.setCompletedJobExecutionAsPreserved(5);

        assertTrue(result.isEmpty());
        verify(jobExecutionRepository).lockCompletedJobExecutions(5);
        verify(jobExecutionRepository, never()).saveAll(anyList());
    }

    // ===========================
    // FAILED JOBS
    // ===========================

    @Test
    @DisplayName("setFailedJobExecutionAsPreserved should update status to PRESERVED and save all failed jobs")
    void setFailedJobExecutionAsPreserved_updatesStatusAndSavesFailedJobs() {
        JobExecution je = new JobExecution();
        je.setStatus(ExecutionStatus.FAILED);

        List<JobExecution> failedJobs = List.of(je);

        when(jobExecutionRepository.lockFailedJobExecutions(5)).thenReturn(failedJobs);
        when(jobExecutionRepository.saveAll(failedJobs)).thenReturn(failedJobs);

        List<JobExecution> result = jobExecutionStateService.setFailedJobExecutionAsPreserved(5);

        assertEquals(1, result.size());
        assertEquals(ExecutionStatus.PRESERVED, result.get(0).getStatus());

        verify(jobExecutionRepository).lockFailedJobExecutions(5);
        verify(jobExecutionRepository).saveAll(failedJobs);
    }

    @Test
    @DisplayName("setFailedJobExecutionAsPreserved should return empty list when none are found")
    void setFailedJobExecutionAsPreserved_emptyList() {
        when(jobExecutionRepository.lockFailedJobExecutions(5)).thenReturn(Collections.emptyList());

        List<JobExecution> result = jobExecutionStateService.setFailedJobExecutionAsPreserved(5);

        assertTrue(result.isEmpty());
        verify(jobExecutionRepository).lockFailedJobExecutions(5);
        verify(jobExecutionRepository, never()).saveAll(anyList());
    }

    // ===========================
    // TIMED OUT JOBS
    // ===========================

    @Test
    @DisplayName("setTimedOutJobExecutionAsPreserved should update status to PRESERVED and save all timed-out jobs")
    void setTimedOutJobExecutionAsPreserved_updatesStatusAndSavesTimedOutJobs() {
        JobExecution je = new JobExecution();
        je.setStatus(ExecutionStatus.TIMED_OUT);

        List<JobExecution> timedOutJobs = List.of(je);

        when(jobExecutionRepository.lockTimedOutJobExecutions(5)).thenReturn(timedOutJobs);
        when(jobExecutionRepository.saveAll(timedOutJobs)).thenReturn(timedOutJobs);

        List<JobExecution> result = jobExecutionStateService.setTimedOutJobExecutionAsPreserved(5);

        assertEquals(1, result.size());
        assertEquals(ExecutionStatus.PRESERVED, result.get(0).getStatus());

        verify(jobExecutionRepository).lockTimedOutJobExecutions(5);
        verify(jobExecutionRepository).saveAll(timedOutJobs);
    }

    @Test
    @DisplayName("setTimedOutJobExecutionAsPreserved should return empty list when none are found")
    void setTimedOutJobExecutionAsPreserved_emptyList() {
        when(jobExecutionRepository.lockTimedOutJobExecutions(5)).thenReturn(Collections.emptyList());

        List<JobExecution> result = jobExecutionStateService.setTimedOutJobExecutionAsPreserved(5);

        assertTrue(result.isEmpty());
        verify(jobExecutionRepository).lockTimedOutJobExecutions(5);
        verify(jobExecutionRepository, never()).saveAll(anyList());
    }
}
