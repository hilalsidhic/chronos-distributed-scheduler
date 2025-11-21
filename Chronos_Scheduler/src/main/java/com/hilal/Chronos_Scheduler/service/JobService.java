package com.hilal.Chronos_Scheduler.service;

import com.hilal.Chronos_Scheduler.entities.Job;
import com.hilal.Chronos_Scheduler.entities.dtos.JobRequestDto;
import com.hilal.Chronos_Scheduler.entities.dtos.JobResponseDto;

import java.util.List;

public interface JobService {
    public JobResponseDto createJob_service(JobRequestDto jobRequestDto);
    public JobResponseDto createRecurringJob_service(JobRequestDto jobRequestDto);
    public JobResponseDto getJobById_service(long id);
    public void deleteJobById_service(long id);
    public List<JobResponseDto> getAllJobs_service();
}
