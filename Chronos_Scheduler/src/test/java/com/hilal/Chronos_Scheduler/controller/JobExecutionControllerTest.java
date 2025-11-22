package com.hilal.Chronos_Scheduler.controller;

import com.hilal.Chronos_Scheduler.entities.dtos.JobExecutionResponseDto;
import com.hilal.Chronos_Scheduler.service.JobExecutionService;
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

class JobExecutionControllerTest {

    @Mock
    private JobExecutionService jobExecutionService;

    @InjectMocks
    private JobExecutionController jobExecutionController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("getExecutionsByJobId should return a list of executions for a valid job ID")
    void getExecutionsByJobId_returnsExecutionsForValidJobId() {
        long jobId = 1L;
        long limit = 10L;
        long offset = 0L;
        List<JobExecutionResponseDto> executions = List.of(new JobExecutionResponseDto(), new JobExecutionResponseDto());

        when(jobExecutionService.getJobExecutionsByJobId_service(jobId, limit, offset)).thenReturn(executions);

        ResponseEntity<List<JobExecutionResponseDto>> response = jobExecutionController.getExecutionsByJobId(jobId, limit, offset);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(executions, response.getBody());
    }

    @Test
    @DisplayName("getExecutionsByJobId should return an empty list when no executions are found")
    void getExecutionsByJobId_returnsEmptyListWhenNoExecutionsFound() {
        long jobId = 1L;
        long limit = 10L;
        long offset = 0L;

        when(jobExecutionService.getJobExecutionsByJobId_service(jobId, limit, offset)).thenReturn(List.of());

        ResponseEntity<List<JobExecutionResponseDto>> response = jobExecutionController.getExecutionsByJobId(jobId, limit, offset);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    @DisplayName("getExecutionsByJobId should throw an exception for an invalid job ID")
    void getExecutionsByJobId_throwsExceptionForInvalidJobId() {
        long jobId = -1L;
        long limit = 10L;
        long offset = 0L;

        when(jobExecutionService.getJobExecutionsByJobId_service(jobId, limit, offset)).thenThrow(new IllegalArgumentException("Invalid job ID"));

        assertThrows(IllegalArgumentException.class, () -> jobExecutionController.getExecutionsByJobId(jobId, limit, offset));
    }

    @Test
    @DisplayName("GetExecutionById should return the execution for a valid execution ID")
    void GetExecutionById_returnsExecutionForValidExecutionId() {
        long executionId = 1L;
        JobExecutionResponseDto execution = new JobExecutionResponseDto();

        when(jobExecutionService.getJobExecutionById_service(executionId)).thenReturn(execution);

        ResponseEntity<JobExecutionResponseDto> response = jobExecutionController.GetExecutionById(executionId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(execution, response.getBody());
    }

    @Test
    @DisplayName("GetExecutionById should throw an exception for an invalid execution ID")
    void GetExecutionById_throwsExceptionForInvalidExecutionId() {
        long executionId = -1L;

        when(jobExecutionService.getJobExecutionById_service(executionId)).thenThrow(new IllegalArgumentException("Invalid execution ID"));

        assertThrows(IllegalArgumentException.class, () -> jobExecutionController.GetExecutionById(executionId));
    }
}
