package com.hilal.Chronos_Scheduler.unit.controller;

import com.hilal.Chronos_Scheduler.controller.JobController;
import com.hilal.Chronos_Scheduler.entities.dtos.JobRequestDto;
import com.hilal.Chronos_Scheduler.entities.dtos.JobResponseDto;
import com.hilal.Chronos_Scheduler.service.JobExecutionService;
import com.hilal.Chronos_Scheduler.service.JobService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JobControllerTest {

    @Mock
    private JobService jobService;

    @Mock
    private JobExecutionService jobExecutionService;

    @InjectMocks
    private JobController jobController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("createJob should return the created job when valid input is provided")
    void createJob_returnsCreatedJobForValidInput() {
        JobRequestDto jobRequestDto = new JobRequestDto();
        JobResponseDto jobResponseDto = new JobResponseDto();

        when(jobService.createJob_service(jobRequestDto)).thenReturn(jobResponseDto);

        ResponseEntity<JobResponseDto> response = jobController.createJob(jobRequestDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(jobResponseDto, response.getBody());
    }

    @Test
    @DisplayName("createRecurringJob should return the created recurring job when valid input is provided")
    void createRecurringJob_returnsCreatedRecurringJobForValidInput() {
        JobRequestDto jobRequestDto = new JobRequestDto();
        JobResponseDto jobResponseDto = new JobResponseDto();

        when(jobService.createRecurringJob_service(jobRequestDto)).thenReturn(jobResponseDto);

        ResponseEntity<JobResponseDto> response = jobController.createRecurringJob(jobRequestDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(jobResponseDto, response.getBody());
    }

    @Test
    @DisplayName("getJobById should return the job when a valid ID is provided")
    void getJobById_returnsJobForValidId() {
        long jobId = 1L;
        JobResponseDto jobResponseDto = new JobResponseDto();

        when(jobService.getJobById_service(jobId)).thenReturn(jobResponseDto);

        ResponseEntity<JobResponseDto> response = jobController.getJobById(jobId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(jobResponseDto, response.getBody());
    }

    @Test
    @DisplayName("getJobById should throw an exception when the job ID does not exist")
    void getJobById_throwsExceptionForNonExistentId() {
        long jobId = 999L;

        when(jobService.getJobById_service(jobId)).thenThrow(new RuntimeException("Job not found"));

        assertThrows(RuntimeException.class, () -> jobController.getJobById(jobId));
    }

    @Test
    @DisplayName("deleteJobById should return success message when a valid ID is provided")
    void deleteJobById_returnsSuccessMessageForValidId() {
        long jobId = 1L;

        ResponseEntity<String> response = jobController.deleteJobById(jobId);

        verify(jobService).deleteJobById_service(jobId);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Job deleted successfully", response.getBody());
    }

    @Test
    @DisplayName("getAllJobs should return a list of jobs")
    void getAllJobs_returnsListOfJobs() {
        List<JobResponseDto> jobs = List.of(new JobResponseDto(), new JobResponseDto());

        when(jobService.getAllJobs_service()).thenReturn(jobs);

        ResponseEntity<List<JobResponseDto>> response = jobController.getAllJobs();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(jobs, response.getBody());
    }

    @Test
    @DisplayName("createJob should throw an exception when input is invalid")
    void createJob_throwsExceptionForInvalidInput() {
        JobRequestDto jobRequestDto = null;

        when(jobService.createJob_service(null)).thenThrow(new IllegalArgumentException("Invalid input"));

        assertThrows(IllegalArgumentException.class, () -> jobController.createJob(jobRequestDto));
    }

    @Test
    @DisplayName("createRecurringJob should throw an exception when input is invalid")
    void createRecurringJob_throwsExceptionForInvalidInput() {
        JobRequestDto jobRequestDto = null;

        when(jobService.createRecurringJob_service(null)).thenThrow(new IllegalArgumentException("Invalid input"));

        assertThrows(IllegalArgumentException.class, () -> jobController.createRecurringJob(jobRequestDto));
    }
}
