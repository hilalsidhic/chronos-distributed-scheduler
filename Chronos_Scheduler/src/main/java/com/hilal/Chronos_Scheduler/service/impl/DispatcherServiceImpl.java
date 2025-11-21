package com.hilal.Chronos_Scheduler.service.impl;

import com.hilal.Chronos_Scheduler.entities.Job;
import com.hilal.Chronos_Scheduler.factories.JobExecutionFactory;
import com.hilal.Chronos_Scheduler.factories.JobFactory;
import com.hilal.Chronos_Scheduler.repository.JobExecutionRepository;
import com.hilal.Chronos_Scheduler.service.DispatcherService;
import com.hilal.Chronos_Scheduler.service.JobExecutionService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.BlockingQueue;

@Service
public class DispatcherServiceImpl implements DispatcherService {

    @Autowired
    private BlockingQueue<Job> jobQueue;

    @Autowired
    private JobExecutionService jobExecutionService;

    @Override
    @PostConstruct
    @Async("jobExecutor")
    public void dispatchJobExecution() {
        while (true){
            try{
                Job job = jobQueue.take();
                jobExecutionService.createJobExecution(job);
            }
            catch (InterruptedException e){
                Thread.currentThread().interrupt();
                break;
            }
        }
        return;
    }
}
