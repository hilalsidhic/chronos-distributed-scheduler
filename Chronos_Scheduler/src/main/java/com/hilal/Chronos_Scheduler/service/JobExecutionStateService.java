package com.hilal.Chronos_Scheduler.service;

import com.hilal.Chronos_Scheduler.entities.JobExecution;

import java.util.List;

public interface JobExecutionStateService {
    public List<JobExecution> setCompletedJobExecutionAsPreserved(int limit);
    public List<JobExecution> setFailedJobExecutionAsPreserved(int limit);
    public List<JobExecution> setTimedOutJobExecutionAsPreserved(int limit);
}
