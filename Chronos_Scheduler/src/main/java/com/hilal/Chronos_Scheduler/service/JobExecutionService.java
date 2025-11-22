package com.hilal.Chronos_Scheduler.service;


import com.hilal.Chronos_Scheduler.entities.Job;
import com.hilal.Chronos_Scheduler.entities.JobExecution;
import com.hilal.Chronos_Scheduler.entities.dtos.JobExecutionResponseDto;
import com.hilal.Chronos_Scheduler.entities.dtos.JobResponseDto;

import java.util.List;

public interface JobExecutionService {
    public List<JobExecutionResponseDto> getJobExecutionsByJobId_service(long jobId, long limit, long offset);
    public JobExecutionResponseDto getJobExecutionById_service(long id);
    public void createJobExecution(Job job);
}
