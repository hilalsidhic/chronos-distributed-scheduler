package com.hilal.Chronos_Scheduler.service;

import com.hilal.Chronos_Scheduler.entities.Job;

import java.util.List;

public interface JobStateService {
    public void setJobAsRunning(Job job);
    public void setJobAsPending(Job job);
    public List<Job> getJobsToExecuteBatch(int limit);
}
