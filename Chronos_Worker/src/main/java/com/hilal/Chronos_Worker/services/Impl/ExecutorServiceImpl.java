package com.hilal.Chronos_Worker.services.Impl;

import com.hilal.Chronos_Worker.engines.HttpEngine;
import com.hilal.Chronos_Worker.engines.WorkerEngine;
import com.hilal.Chronos_Worker.entities.JobExecution;
import com.hilal.Chronos_Worker.entities.dtos.HttpResult;
import com.hilal.Chronos_Worker.entities.dtos.RunningJobContext;
import com.hilal.Chronos_Worker.entities.dtos.WorkerPayload;
import com.hilal.Chronos_Worker.entities.enums.ExecutionStatus;
import com.hilal.Chronos_Worker.exceptions.types.NotFoundException;
import com.hilal.Chronos_Worker.repositories.JobExecutionRepository;
import com.hilal.Chronos_Worker.services.ExecutorService;
import io.micrometer.core.instrument.Counter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.concurrent.Future;

@Service
public class ExecutorServiceImpl implements ExecutorService {

    @Autowired
    private JobExecutionRepository jobExecutionRepository;

    @Autowired
    private HttpEngine httpEngine;

    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Autowired
    private WorkerEngine workerEngine;

    @Autowired
    private Counter jobsCompleted;

    @Autowired
    private Counter jobsFailed;

    @Override
    public void executeJob(JobExecution jobExecution, WorkerPayload workerPayload) {
        if(jobExecution == null){
            throw new NotFoundException("JobExecution not found for execution");
        }
        RunningJobContext runningJobContext =
                        RunningJobContext.builder()
                        .executionId(jobExecution.getId())
                        .jobExecution(jobExecution)
                        .startedAt(jobExecution.getStartedAt())
                        .maxExecutionSeconds(jobExecution.getMaxExecutionTime())
                        .build();
        Future<?> future = threadPoolTaskExecutor.submit(() -> runJob(jobExecution, workerPayload));
        runningJobContext.setFuture(future);
        workerEngine.addRunningJob(jobExecution.getId(),runningJobContext);
        return;
    }

    @Override
    @Transactional
    public void updateJob(JobExecution jobExecution, String logUpdate,ExecutionStatus status) {
        if(jobExecution == null){
            throw new NotFoundException("JobExecution not found for update");
        }
        jobExecution.setLog(jobExecution.getLog() + "\n" + logUpdate);
        jobExecution.setStatus(status);
        jobExecution.setFinishedAt(OffsetDateTime.now());
        jobExecutionRepository.save(jobExecution);
    }

    @Override
    public void runJob(JobExecution jobExecution, WorkerPayload workerPayload) {
        try{
            HttpResult httpResult = httpEngine.execute(workerPayload);
            if(jobExecutionRepository.findStatusById(jobExecution.getId()) != ExecutionStatus.RUNNING){
                return;
            }
            String logUpdate = "HTTP Status: " + httpResult.getStatusCode() + "\nResponse Body: " + httpResult.getResponseBody();
            if (httpResult.getStatusCode() >= 200 && httpResult.getStatusCode() < 300) {
                updateJob(jobExecution,logUpdate,ExecutionStatus.SUCCESS);
                jobsCompleted.increment();
            }
            else if(httpResult.getStatusCode() == 408){
                updateJob(jobExecution,logUpdate,ExecutionStatus.TIMED_OUT);
                jobsFailed.increment();
            }
            else {
                updateJob(jobExecution,logUpdate,ExecutionStatus.FAILED);
                jobsFailed.increment();
            }
        }finally {
            workerEngine.markJobAsCompleted(jobExecution.getId());
        }
    }
}
