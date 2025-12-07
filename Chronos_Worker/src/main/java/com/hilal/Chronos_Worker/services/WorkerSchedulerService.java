package com.hilal.Chronos_Worker.services;

import com.hilal.Chronos_Worker.entities.JobExecution;

import java.util.List;

public interface WorkerSchedulerService {
    public void fetchAndExecuteJobs();
    public List<JobExecution> getJobsAndSetToRunning();
    public void reconcileWorkerState();
}
