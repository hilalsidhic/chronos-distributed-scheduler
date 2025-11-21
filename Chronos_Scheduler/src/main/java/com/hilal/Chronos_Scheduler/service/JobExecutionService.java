package com.hilal.Chronos_Scheduler.service;


import com.hilal.Chronos_Scheduler.entities.Job;
import com.hilal.Chronos_Scheduler.entities.dtos.JobResponseDto;

public interface JobExecutionService {
    public JobResponseDto getJobExecutionsById_service(long id);
    public void logJobExecution(long jobId, String status, String message);
    public void updateJobExecutionStatus(long executionId, String status);
    public void markJobExecutionAsFailed(long executionId, String errorMessage);
    public void createJobExecution(Job job);
}
