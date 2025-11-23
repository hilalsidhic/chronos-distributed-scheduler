package com.hilal.Chronos_Scheduler.unit.service;

import com.hilal.Chronos_Scheduler.entities.Job;
import com.hilal.Chronos_Scheduler.entities.JobExecution;
import com.hilal.Chronos_Scheduler.entities.dtos.JobExecutionResponseDto;
import com.hilal.Chronos_Scheduler.entities.mapper.JobExecutionMapper;
import com.hilal.Chronos_Scheduler.factories.JobExecutionFactory;
import com.hilal.Chronos_Scheduler.repository.JobExecutionRepository;
import com.hilal.Chronos_Scheduler.service.impl.JobExecutionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import java.util.concurrent.BlockingQueue;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JobExecutionServiceImplTest {

    @Mock
    private JobExecutionRepository jobExecutionRepository;

    @Mock
    private JobExecutionFactory jobExecutionFactory;

    @Mock
    private BlockingQueue<Job> jobQueue;

    @InjectMocks
    private JobExecutionServiceImpl jobExecutionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // -------------------------------------------------------------------------
    @Test
    @DisplayName("getJobExecutionsByJobId_service returns mapped results when executions exist")
    void getJobExecutionsByJobId_returnsMappedResults() {
        long jobId = 1L;
        long limit = 10L;
        long offset = 0L;

        JobExecution execution = new JobExecution();
        List<JobExecution> executions = List.of(execution);

        JobExecutionResponseDto mappedDto = new JobExecutionResponseDto();

        when(jobExecutionRepository.findByJobId(jobId, limit, offset)).thenReturn(executions);

        try (MockedStatic<JobExecutionMapper> mocked = mockStatic(JobExecutionMapper.class)) {

            mocked.when(() -> JobExecutionMapper.toJobExecutionResponseDto(execution))
                    .thenReturn(mappedDto);

            List<JobExecutionResponseDto> result =
                    jobExecutionService.getJobExecutionsByJobId_service(jobId, limit, offset);

            assertEquals(1, result.size());
            assertSame(mappedDto, result.get(0));

            verify(jobExecutionRepository).findByJobId(jobId, limit, offset);
            mocked.verify(() -> JobExecutionMapper.toJobExecutionResponseDto(execution));
        }
    }

    // -------------------------------------------------------------------------
    @Test
    @DisplayName("getJobExecutionsByJobId_service returns empty list when nothing found")
    void getJobExecutionsByJobId_returnsEmptyList() {
        long jobId = 1L;
        long limit = 10;
        long offset = 0;

        when(jobExecutionRepository.findByJobId(jobId, limit, offset))
                .thenReturn(Collections.emptyList());

        List<JobExecutionResponseDto> result =
                jobExecutionService.getJobExecutionsByJobId_service(jobId, limit, offset);

        assertTrue(result.isEmpty());
        verify(jobExecutionRepository).findByJobId(jobId, limit, offset);
    }

    // -------------------------------------------------------------------------
    @Test
    @DisplayName("getJobExecutionById_service returns mapped DTO when execution exists")
    void getJobExecutionById_returnsMappedDto() {
        long id = 1L;
        JobExecution execution = new JobExecution();
        JobExecutionResponseDto mappedDto = new JobExecutionResponseDto();

        when(jobExecutionRepository.findById(id)).thenReturn(Optional.of(execution));

        try (MockedStatic<JobExecutionMapper> mocked = mockStatic(JobExecutionMapper.class)) {

            mocked.when(() -> JobExecutionMapper.toJobExecutionResponseDto(execution))
                    .thenReturn(mappedDto);

            JobExecutionResponseDto result =
                    jobExecutionService.getJobExecutionById_service(id);

            assertSame(mappedDto, result);

            verify(jobExecutionRepository).findById(id);
            mocked.verify(() -> JobExecutionMapper.toJobExecutionResponseDto(execution));
        }
    }

    // -------------------------------------------------------------------------
    @Test
    @DisplayName("getJobExecutionById_service throws when not found")
    void getJobExecutionById_throwsWhenMissing() {
        long id = 1L;

        when(jobExecutionRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> jobExecutionService.getJobExecutionById_service(id));

        verify(jobExecutionRepository).findById(id);
    }

    // -------------------------------------------------------------------------
    @Test
    @DisplayName("createJobExecution saves the JobExecution returned by factory")
    void createJobExecution_savesExecution() {
        Job job = new Job();
        JobExecution execution = new JobExecution();

        when(jobExecutionFactory.createJobExecution(job)).thenReturn(execution);

        jobExecutionService.createJobExecution(job);

        verify(jobExecutionRepository).save(execution);
    }
}
