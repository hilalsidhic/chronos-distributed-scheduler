package com.hilal.Chronos_Scheduler.service.impl;

import com.hilal.Chronos_Scheduler.entities.Job;
import com.hilal.Chronos_Scheduler.entities.dtos.JobResponseDto;
import com.hilal.Chronos_Scheduler.entities.enums.ExecutionStatus;
import com.hilal.Chronos_Scheduler.entities.enums.Status;
import com.hilal.Chronos_Scheduler.factories.JobExecutionFactory;
import com.hilal.Chronos_Scheduler.repository.JobExecutionRepository;
import com.hilal.Chronos_Scheduler.service.JobExecutionService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;

@Service
public class JobExecutionServiceImpl implements JobExecutionService {

    @Autowired
    private JobExecutionRepository jobExecutionRepository;

    @Autowired
    private JobExecutionFactory jobExecutionFactory;

    @Autowired
    private BlockingQueue<Job> jobQueue;

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
    public void markJobExecutionAsFailed(long executionId, String errorMessage) {

    }

    @Override
    public void createJobExecution(Job job) {
        jobExecutionRepository.save(jobExecutionFactory.createJobExecution(job));
        return;
    }
}
