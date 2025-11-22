package com.hilal.Chronos_Scheduler.service;

import com.hilal.Chronos_Scheduler.entities.Job;
import com.hilal.Chronos_Scheduler.entities.dtos.JobRequestDto;
import com.hilal.Chronos_Scheduler.entities.dtos.JobResponseDto;
import com.hilal.Chronos_Scheduler.factories.JobFactory;
import com.hilal.Chronos_Scheduler.entities.mapper.JobMapper;
import com.hilal.Chronos_Scheduler.repository.JobRepository;
import com.hilal.Chronos_Scheduler.service.impl.JobServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class JobServiceImplTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private JobFactory jobFactory;

    @InjectMocks
    private JobServiceImpl jobService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("createJob_service should save and return a new job")
    void createJob_service_savesAndReturnsNewJob() {
        JobRequestDto jobRequestDto = new JobRequestDto();
        Job job = new Job();
        JobResponseDto jobResponseDto = new JobResponseDto();

        when(jobFactory.createJob(jobRequestDto, false)).thenReturn(job);
        when(jobRepository.save(job)).thenReturn(job);

        try (MockedStatic<JobMapper> mocked = mockStatic(JobMapper.class)) {
            mocked.when(() -> JobMapper.mapJobToJobResponseDto(job)).thenReturn(jobResponseDto);

            JobResponseDto result = jobService.createJob_service(jobRequestDto);

            Assertions.assertEquals(result, jobResponseDto);
            verify(jobRepository).save(job);
            mocked.verify(() -> JobMapper.mapJobToJobResponseDto(job));
        }
    }

    @Test
    @DisplayName("createRecurringJob_service should save and return a new recurring job")
    void createRecurringJob_service_savesAndReturnsNewRecurringJob() {
        JobRequestDto jobRequestDto = new JobRequestDto();
        Job job = new Job();
        JobResponseDto jobResponseDto = new JobResponseDto();

        when(jobFactory.createJob(jobRequestDto, true)).thenReturn(job);
        when(jobRepository.save(job)).thenReturn(job);

        try (MockedStatic<JobMapper> mocked = mockStatic(JobMapper.class)) {
            mocked.when(() -> JobMapper.mapJobToJobResponseDto(job)).thenReturn(jobResponseDto);

            JobResponseDto result = jobService.createRecurringJob_service(jobRequestDto);

            Assertions.assertEquals(result, jobResponseDto);
            verify(jobRepository).save(job);
            mocked.verify(() -> JobMapper.mapJobToJobResponseDto(job));
        }
    }

    @Test
    @DisplayName("getJobById_service should return job when found")
    void getJobById_service_returnsJobWhenFound() {
        long jobId = 1L;
        Job job = new Job();
        JobResponseDto jobResponseDto = new JobResponseDto();

        when(jobRepository.findById(jobId)).thenReturn(Optional.of(job));

        try (MockedStatic<JobMapper> mocked = mockStatic(JobMapper.class)) {
            mocked.when(() -> JobMapper.mapJobToJobResponseDto(job)).thenReturn(jobResponseDto);

            JobResponseDto result = jobService.getJobById_service(jobId);

            Assertions.assertEquals(result, jobResponseDto);
            verify(jobRepository).findById(jobId);
            mocked.verify(() -> JobMapper.mapJobToJobResponseDto(job));
        }
    }

    @Test
    @DisplayName("getJobById_service should throw exception when job not found")
    void getJobById_service_throwsExceptionWhenJobNotFound() {
        long jobId = 1L;

        when(jobRepository.findById(jobId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> jobService.getJobById_service(jobId));
        verify(jobRepository).findById(jobId);
    }

    @Test
    @DisplayName("deleteJobById_service should delete job when it exists")
    void deleteJobById_service_deletesJobWhenItExists() {
        long jobId = 1L;

        when(jobRepository.existsById(jobId)).thenReturn(true);

        jobService.deleteJobById_service(jobId);

        verify(jobRepository).deleteById(jobId);
    }

    @Test
    @DisplayName("deleteJobById_service should throw exception when job does not exist")
    void deleteJobById_service_throwsExceptionWhenJobDoesNotExist() {
        long jobId = 1L;

        when(jobRepository.existsById(jobId)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> jobService.deleteJobById_service(jobId));
        verify(jobRepository, never()).deleteById(jobId);
    }

    @Test
    @DisplayName("getAllJobs_service should return all jobs")
    void getAllJobs_service_returnsAllJobs() {
        Job job = new Job();
        List<Job> jobs = List.of(job);
        JobResponseDto jobResponseDto = new JobResponseDto();

        when(jobRepository.findAll()).thenReturn(jobs);

        try (MockedStatic<JobMapper> mocked = mockStatic(JobMapper.class)) {
            mocked.when(() -> JobMapper.mapJobToJobResponseDto(job)).thenReturn(jobResponseDto);

            var result = jobService.getAllJobs_service();

            Assertions.assertEquals(result.size(), 1);
            verify(jobRepository).findAll();
            mocked.verify(() -> JobMapper.mapJobToJobResponseDto(job));
        }
    }
}