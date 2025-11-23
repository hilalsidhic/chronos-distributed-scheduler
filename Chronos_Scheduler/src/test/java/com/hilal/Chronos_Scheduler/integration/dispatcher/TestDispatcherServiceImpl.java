package com.hilal.Chronos_Scheduler.integration.dispatcher;

import com.hilal.Chronos_Scheduler.entities.Job;
import com.hilal.Chronos_Scheduler.service.DispatcherService;
import com.hilal.Chronos_Scheduler.service.JobExecutionService;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.concurrent.BlockingQueue;

@Service
@Primary   // Overrides real DispatcherServiceImpl for tests
public class TestDispatcherServiceImpl implements DispatcherService {

    private final BlockingQueue<Job> jobQueue;
    private final JobExecutionService jobExecutionService;

    public TestDispatcherServiceImpl(BlockingQueue<Job> jobQueue,
                                     JobExecutionService jobExecutionService) {
        this.jobQueue = jobQueue;
        this.jobExecutionService = jobExecutionService;
    }

    @Override
    public void dispatchJobExecution() {
        Job job = jobQueue.poll();
        if (job != null) {
            jobExecutionService.createJobExecution(job);
        }
    }
}
