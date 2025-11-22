package com.hilal.Chronos_Scheduler.service.impl;

import com.hilal.Chronos_Scheduler.entities.Job;
import com.hilal.Chronos_Scheduler.entities.JobExecution;
import com.hilal.Chronos_Scheduler.entities.dtos.JobExecutionResponseDto;
import com.hilal.Chronos_Scheduler.entities.dtos.JobResponseDto;
import com.hilal.Chronos_Scheduler.entities.enums.ExecutionStatus;
import com.hilal.Chronos_Scheduler.entities.enums.Status;
import com.hilal.Chronos_Scheduler.entities.mapper.JobExecutionMapper;
import com.hilal.Chronos_Scheduler.entities.mapper.JobMapper;
import com.hilal.Chronos_Scheduler.factories.JobExecutionFactory;
import com.hilal.Chronos_Scheduler.repository.JobExecutionRepository;
import com.hilal.Chronos_Scheduler.service.JobExecutionService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

@Service
public class JobExecutionServiceImpl implements JobExecutionService {

    @Autowired
    private JobExecutionRepository jobExecutionRepository;

    @Autowired
    private JobExecutionFactory jobExecutionFactory;

    @Autowired
    private BlockingQueue<Job> jobQueue;

    @Override
    public List<JobExecutionResponseDto> getJobExecutionsByJobId_service(long jobId, long limit, long offset) {
        List<JobExecution> jobExecutions =  jobExecutionRepository.findByJobId(jobId,limit,offset);
        return jobExecutions.stream()
                .map(JobExecutionMapper::toJobExecutionResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public JobExecutionResponseDto getJobExecutionById_service(long id) {
        JobExecution jobExecution = jobExecutionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("JobExecution not found"));
        return JobExecutionMapper.toJobExecutionResponseDto(jobExecution);
    }

    @Override
    public void createJobExecution(Job job) {
        jobExecutionRepository.save(jobExecutionFactory.createJobExecution(job));
        return;
    }
}
