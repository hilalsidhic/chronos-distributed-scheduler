package com.hilal.Chronos_Scheduler.controller;

import com.hilal.Chronos_Scheduler.entities.dtos.JobRequestDto;
import com.hilal.Chronos_Scheduler.entities.dtos.JobResponseDto;
import com.hilal.Chronos_Scheduler.service.JobExecutionService;
import com.hilal.Chronos_Scheduler.service.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class JobController {

    @Autowired
    private JobService jobService;

    @Autowired
    private JobExecutionService jobExecutionService;

    @PostMapping("/jobs")
    public ResponseEntity<JobResponseDto> createJob(@RequestBody JobRequestDto jobRequestDto) {
        return ResponseEntity.ok(new JobResponseDto());
    }

    @PostMapping("/jobs/recurring")
    public ResponseEntity<JobResponseDto> createRecurringJob(@RequestBody JobRequestDto jobRequestDto) {
        return ResponseEntity.ok(new JobResponseDto());
    }

    @GetMapping("/jobs/{id}")
    public ResponseEntity<JobResponseDto> getJobById(@PathVariable long id) {
        return ResponseEntity.ok(new JobResponseDto());
    }

    @DeleteMapping("/jobs/{id}")
    public ResponseEntity<JobResponseDto> deleteJobById(@PathVariable long id) {
        return ResponseEntity.ok(new JobResponseDto());
    }

    @GetMapping("/jobs/{id}/executions")
    public ResponseEntity<JobResponseDto> getExecutionsById(@PathVariable long id) {
        return ResponseEntity.ok(new JobResponseDto());
    }

    @GetMapping("/jobs")
    public ResponseEntity<JobResponseDto> getAllJobs() {
        return ResponseEntity.ok(new JobResponseDto());
    }

    @PostMapping("/jobs/{id}/retry")
    public ResponseEntity<JobResponseDto> retryJobById(@PathVariable long id) {
        return ResponseEntity.ok(new JobResponseDto());
    }
}
