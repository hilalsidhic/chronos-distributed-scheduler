package com.hilal.Chronos_Scheduler.controller;

import com.hilal.Chronos_Scheduler.entities.dtos.ExecutionStatsResponse;
import com.hilal.Chronos_Scheduler.entities.dtos.JobExecutionResponseDto;
import com.hilal.Chronos_Scheduler.entities.dtos.PaginatedExecutionResponse;
import com.hilal.Chronos_Scheduler.service.JobExecutionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class JobExecutionController {

    @Autowired
    private JobExecutionService jobExecutionService;

    @GetMapping("/jobs/{id}/executions")
    public ResponseEntity<List<JobExecutionResponseDto>> getExecutionsByJobId(
            @PathVariable long id,
            @RequestParam(defaultValue = "10") long limit,
            @RequestParam(defaultValue = "0") long offset) {
        List<JobExecutionResponseDto> executions = jobExecutionService.getJobExecutionsByJobId_service(id, limit, offset);
        return ResponseEntity.ok(executions);
    }

    @GetMapping("/executions/{id}")
    public ResponseEntity<JobExecutionResponseDto> GetExecutionById(@PathVariable long id) {
        JobExecutionResponseDto execution = jobExecutionService.getJobExecutionById_service(id);
        return ResponseEntity.ok(execution);
    }

    @GetMapping("/executions/")
    public ResponseEntity<PaginatedExecutionResponse> getAllExecutions(
            @RequestParam(defaultValue = "10") long limit,
            @RequestParam(defaultValue = "0") long offset) {
        PaginatedExecutionResponse response = jobExecutionService.getAllJobExecutions(limit, offset);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/executions/stats")
    public ResponseEntity<ExecutionStatsResponse> getExecutionStats() {
        return ResponseEntity.ok(jobExecutionService.getExecutionStats());
    }

}
