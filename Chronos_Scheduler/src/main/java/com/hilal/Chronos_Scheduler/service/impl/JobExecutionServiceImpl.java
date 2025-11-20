package com.hilal.Chronos_Scheduler.service.impl;

import com.hilal.Chronos_Scheduler.entities.dtos.JobResponseDto;
import com.hilal.Chronos_Scheduler.repository.JobExecutionRepository;
import com.hilal.Chronos_Scheduler.service.JobExecutionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class JobExecutionServiceImpl implements JobExecutionService {

    @Autowired
    private JobExecutionRepository jobExecutionRepository;

    @Override
    public JobResponseDto getJobExecutionsById_service(long id) {
        return null;
    }

    @Override
    public void logJobExecution(long jobId, String status, String message) {

    }

    @Override
    public void updateJobExecutionStatus(long executionId, String status) {

    }

    @Override
    public void createJobExecution(long jobId) {

    }

    @Override
    public void markJobExecutionAsFailed(long executionId, String errorMessage) {

    }
}
