package com.hilal.Chronos_Scheduler.service.impl;

import com.hilal.Chronos_Scheduler.entities.dtos.JobRequestDto;
import com.hilal.Chronos_Scheduler.entities.dtos.JobResponseDto;
import com.hilal.Chronos_Scheduler.factories.JobFactory;
import com.hilal.Chronos_Scheduler.repository.JobRepository;
import com.hilal.Chronos_Scheduler.service.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class JobServiceImpl implements JobService {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private JobFactory jobFactory;

    @Override
    public JobResponseDto createJob_service(JobRequestDto jobRequestDto) {
        jobRepository.save(jobFactory.createJob(jobRequestDto, false));
        return new JobResponseDto();
    }

    @Override
    public JobResponseDto createRecurringJob_service(JobRequestDto jobRequestDto) {
        return null;
    }

    @Override
    public JobResponseDto getJobById_service(long id) {
        return null;
    }

    @Override
    public JobResponseDto deleteJobById_service(long id) {
        return null;
    }


    @Override
    public JobResponseDto getAllJobs_service() {
        return null;
    }

    @Override
    public JobResponseDto retryJobById_service(long id) {
        return null;
    }
}
