package com.hilal.Chronos_Worker.services;

import com.hilal.Chronos_Worker.entities.JobExecution;

public interface WorkerDispatcherService {
    public void dispatchJobs();
    public void startJobDispatch(JobExecution jobExecution);
}
