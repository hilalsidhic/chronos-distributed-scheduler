package com.hilal.Chronos_Scheduler.factories;

import com.hilal.Chronos_Scheduler.entities.Job;
import com.hilal.Chronos_Scheduler.entities.JobExecution;

public interface JobExecutionFactory {
    public JobExecution createJobExecution(Job job);
}
