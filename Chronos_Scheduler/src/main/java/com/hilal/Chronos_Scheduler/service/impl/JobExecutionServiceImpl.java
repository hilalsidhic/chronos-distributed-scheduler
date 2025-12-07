package com.hilal.Chronos_Scheduler.service.impl;

import com.hilal.Chronos_Scheduler.entities.Job;
import com.hilal.Chronos_Scheduler.entities.JobExecution;
import com.hilal.Chronos_Scheduler.entities.dtos.ExecutionStatsResponse;
import com.hilal.Chronos_Scheduler.entities.dtos.JobExecutionResponseDto;
import com.hilal.Chronos_Scheduler.entities.dtos.JobResponseDto;
import com.hilal.Chronos_Scheduler.entities.dtos.PaginatedExecutionResponse;
import com.hilal.Chronos_Scheduler.entities.enums.ExecutionStatus;
import com.hilal.Chronos_Scheduler.entities.enums.Status;
import com.hilal.Chronos_Scheduler.entities.mapper.JobExecutionMapper;
import com.hilal.Chronos_Scheduler.entities.mapper.JobMapper;
import com.hilal.Chronos_Scheduler.exceptions.types.NotFoundException;
import com.hilal.Chronos_Scheduler.factories.JobExecutionFactory;
import com.hilal.Chronos_Scheduler.repository.JobExecutionRepository;
import com.hilal.Chronos_Scheduler.service.JobExecutionService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${scheduler.executions.recent-limit:10}")
    private int recentLimit;

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
                .orElseThrow(() -> new NotFoundException("JobExecution not found"));
        return JobExecutionMapper.toJobExecutionResponseDto(jobExecution);
    }

    @Override
    public void createJobExecution(Job job) {
        jobExecutionRepository.save(jobExecutionFactory.createJobExecution(job));
        return;
    }

    @Override
    public PaginatedExecutionResponse getAllJobExecutions(long limit, long offset) {

        List<JobExecution> jobExecutions =
                jobExecutionRepository.findAllJobExecutions(limit, offset);

        long total = jobExecutionRepository.countAllJobExecutions();

        List<JobExecutionResponseDto> items = jobExecutions.stream()
                .map(JobExecutionMapper::toJobExecutionResponseDto)
                .collect(Collectors.toList());

        boolean hasNext = (offset + limit) < total;
        return PaginatedExecutionResponse.builder()
                .items(items)
                .total(total)
                .limit(limit)
                .offset(offset)
                .hasNext(hasNext)
                .build();
    }

    @Override
    public ExecutionStatsResponse getExecutionStats() {

        long total = jobExecutionRepository.countAllJobExecutions();
        long success = jobExecutionRepository.countByStatus("SUCCESS");
        long failed = jobExecutionRepository.countByStatus("FAILED");
        long running = jobExecutionRepository.countByStatus("RUNNING");
        long stuck = jobExecutionRepository.countByStatus("STUCK");
        long timedOut = jobExecutionRepository.countByStatus("TIMED_OUT");

        List<JobExecution> recent = jobExecutionRepository.findAllJobExecutions(10,0);
        List<JobExecutionResponseDto> recentDtos = recent.stream()
                .map(JobExecutionMapper::toJobExecutionResponseDto)
                .toList();

        return ExecutionStatsResponse.builder()
                .totalExecutions(total)
                .totalSuccess(success)
                .totalFailed(failed)
                .totalRunning(running)
                .totalStuck(stuck)
                .totalTimedOut(timedOut)
                .recentExecutions(recentDtos)
                .build();
    }
}
