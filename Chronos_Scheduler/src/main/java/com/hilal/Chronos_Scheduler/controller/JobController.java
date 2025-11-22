package com.hilal.Chronos_Scheduler.controller;

import com.hilal.Chronos_Scheduler.entities.dtos.JobRequestDto;
import com.hilal.Chronos_Scheduler.entities.dtos.JobResponseDto;
import com.hilal.Chronos_Scheduler.service.JobExecutionService;
import com.hilal.Chronos_Scheduler.service.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class JobController {

    @Autowired
    private JobService jobService;

    @Autowired
    private JobExecutionService jobExecutionService;

    @PostMapping("/jobs")
    public ResponseEntity<JobResponseDto> createJob(@RequestBody JobRequestDto jobRequestDto) {
        JobResponseDto job = jobService.createJob_service(jobRequestDto);
        return ResponseEntity.ok(job);
    }

    @PostMapping("/jobs/recurring")
    public ResponseEntity<JobResponseDto> createRecurringJob(@RequestBody JobRequestDto jobRequestDto) {
        JobResponseDto job = jobService.createRecurringJob_service(jobRequestDto);
        return ResponseEntity.ok(job);
    }

    @GetMapping("/jobs/{id}")
    public ResponseEntity<JobResponseDto> getJobById(@PathVariable long id) {
        JobResponseDto job = jobService.getJobById_service(id);
        return ResponseEntity.ok(job);
    }

    @DeleteMapping("/jobs/{id}")
    public ResponseEntity<String> deleteJobById(@PathVariable long id) {
        jobService.deleteJobById_service(id);
        return ResponseEntity.ok().body("Job deleted successfully");
    }

    @GetMapping("/jobs")
    public ResponseEntity<List<JobResponseDto>> getAllJobs() {
        List<JobResponseDto> jobs = jobService.getAllJobs_service();
        return ResponseEntity.ok(jobs);
    }
}
