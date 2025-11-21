package com.hilal.Chronos_Scheduler.service.impl;

import com.hilal.Chronos_Scheduler.entities.Job;
import com.hilal.Chronos_Scheduler.entities.enums.Status;
import com.hilal.Chronos_Scheduler.repository.JobRepository;
import com.hilal.Chronos_Scheduler.service.JobStateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class JobStateServiceImpl implements JobStateService {

    @Autowired
    private JobRepository jobRepository;

    @Override
    @Transactional
    public void setJobAsRunning(Job job) {
        job.setStatus(Status.RUNNING);
        jobRepository.save(job);
    }

    @Override
    @Transactional
    public void setJobAsPending(Job job) {
        job.setStatus(Status.PENDING);
        jobRepository.save(job);
    }

    @Override
    @Transactional
    public List<Job> getJobsToExecuteBatch(int limit){
        List<Job> jobs = jobRepository.lockPendingJobs(limit);
        for(Job job : jobs) {
            job.setStatus(Status.RESERVED);
            job.setReservedAt(OffsetDateTime.now());
        }
        jobRepository.saveAll(jobs);
        return jobs;
    }

}
