package com.hilal.Chronos_Scheduler.factories.Impl;

import com.hilal.Chronos_Scheduler.entities.Job;
import com.hilal.Chronos_Scheduler.entities.JobExecution;
import com.hilal.Chronos_Scheduler.entities.enums.ExecutionStatus;
import com.hilal.Chronos_Scheduler.factories.JobExecutionFactory;
import org.springframework.stereotype.Service;

@Service
public class JobExecutionFactoryImpl implements JobExecutionFactory {

    @Override
    public JobExecution createJobExecution(Job job) {
        return JobExecution.builder()
                .job(job)
                .status(ExecutionStatus.PENDING)
                .retryNumber(job.getRetryCount())
                .build();
    }
}
