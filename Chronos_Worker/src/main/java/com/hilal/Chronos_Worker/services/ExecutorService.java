package com.hilal.Chronos_Worker.services;

import com.hilal.Chronos_Worker.entities.JobExecution;
import com.hilal.Chronos_Worker.entities.dtos.WorkerPayload;
import com.hilal.Chronos_Worker.entities.enums.ExecutionStatus;

public interface ExecutorService {
    public void executeJob(JobExecution jobExecution, WorkerPayload workerPayload);
    public void updateJob(JobExecution jobExecution, String logUpdate, ExecutionStatus status);
    public void runJob(JobExecution jobExecution, WorkerPayload workerPayload);
}
