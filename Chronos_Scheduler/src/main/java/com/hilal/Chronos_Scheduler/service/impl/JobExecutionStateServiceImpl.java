package com.hilal.Chronos_Scheduler.service.impl;

import com.hilal.Chronos_Scheduler.entities.JobExecution;
import com.hilal.Chronos_Scheduler.entities.enums.ExecutionStatus;
import com.hilal.Chronos_Scheduler.repository.JobExecutionRepository;
import com.hilal.Chronos_Scheduler.service.JobExecutionStateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class JobExecutionStateServiceImpl implements JobExecutionStateService {

    @Autowired
    private JobExecutionRepository jobExecutionRepository;

    @Override
    @Transactional
    public List<JobExecution> setCompletedJobExecutionAsPreserved(int limit) {
        List<JobExecution> completedJobs = jobExecutionRepository.lockCompletedJobExecutions(limit);
        if (completedJobs.isEmpty()) return completedJobs;
        for(JobExecution jobExecution : completedJobs) {
            jobExecution.setPreserved(true);
        }
        return jobExecutionRepository.saveAll(completedJobs);
    }

    @Override
    @Transactional
    public List<JobExecution> setFailedJobExecutionAsPreserved(int limit) {
        List<JobExecution> failedJobs = jobExecutionRepository.lockFailedJobExecutions(limit);
        if(failedJobs.isEmpty()) return failedJobs;
        for(JobExecution jobExecution : failedJobs) {
            jobExecution.setPreserved(true);
        }
        return jobExecutionRepository.saveAll(failedJobs);
    }

    @Override
    @Transactional
    public List<JobExecution> setTimedOutJobExecutionAsPreserved(int limit) {
        List<JobExecution> timedOutJobs = jobExecutionRepository.lockTimedOutJobExecutions(limit);
        if(timedOutJobs.isEmpty()) return timedOutJobs;
        for(JobExecution jobExecution : timedOutJobs) {
            jobExecution.setPreserved(true);
        }
        return jobExecutionRepository.saveAll(timedOutJobs);
    }
}
