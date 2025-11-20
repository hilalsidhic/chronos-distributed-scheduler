package com.hilal.Chronos_Scheduler.service;

import com.hilal.Chronos_Scheduler.entities.dtos.JobRequestDto;
import com.hilal.Chronos_Scheduler.entities.dtos.JobResponseDto;

public interface JobService {
    public JobResponseDto createJob_service(JobRequestDto jobRequestDto);
    public JobResponseDto createRecurringJob_service(JobRequestDto jobRequestDto);
    public JobResponseDto getJobById_service(long id);
    public JobResponseDto deleteJobById_service(long id);
    public JobResponseDto getAllJobs_service();
    public JobResponseDto retryJobById_service(long id);
}
