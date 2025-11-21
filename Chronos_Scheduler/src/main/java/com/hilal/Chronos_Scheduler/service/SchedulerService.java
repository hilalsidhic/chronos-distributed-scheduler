package com.hilal.Chronos_Scheduler.service;

import com.hilal.Chronos_Scheduler.entities.Job;
import com.hilal.Chronos_Scheduler.entities.JobExecution;

import java.util.List;

public interface SchedulerService {
    public void getJobsToExecute_service();
    public void resetStatusReservedJobs_service();

    public void getCompletedJobExecutions_service();
    public void getFailedJobExecutions_service();
    public void getTimedOutJobExecutions_service();
}
