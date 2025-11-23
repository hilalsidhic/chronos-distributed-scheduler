package com.hilal.Chronos_Scheduler.integration.dispatcher;

import com.hilal.Chronos_Scheduler.entities.Job;
import com.hilal.Chronos_Scheduler.entities.JobExecution;
import com.hilal.Chronos_Scheduler.integration.BaseIntegrationTest;
import com.hilal.Chronos_Scheduler.repository.JobExecutionRepository;
import com.hilal.Chronos_Scheduler.repository.JobRepository;
import com.hilal.Chronos_Scheduler.service.DispatcherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.BlockingQueue;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
        "dispatcher.enabled=false",
        "dispatcher.test-mode=true",
        "scheduler.enabled=false"
})
class DispatcherIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private BlockingQueue<Job> jobQueue;

    @Autowired
    private DispatcherService dispatcherService;

    @Autowired
    private JobExecutionRepository jobExecutionRepository;

    @Autowired
    private JobRepository jobRepository;

    @BeforeEach
    void setup() {
        // Clear DB
        jobExecutionRepository.deleteAll();
        jobRepository.deleteAll();

        // Clear queue
        jobQueue.clear();
    }
    // 1️⃣ Basic test - already working
    @Test
    void dispatcher_should_process_one_job() {
        Job job = jobRepository.save(
                Job.builder().name("dispatch-test").maxRetry(3).isEnabled(true).build()
        );

        jobQueue.offer(job);

        dispatcherService.dispatchJobExecution();

        assertEquals(1, jobExecutionRepository.count());
    }

    // 2️⃣ Queue empty
    @Test
    void dispatcher_should_do_nothing_when_queue_empty() {

        dispatcherService.dispatchJobExecution();

        assertEquals(0, jobExecutionRepository.count());
    }

    // 3️⃣ Process only one job in test-mode
    @Test
    void dispatcher_should_process_only_one_job_in_test_mode() {

        Job job1 = jobRepository.save(Job.builder().name("j1").maxRetry(3).isEnabled(true).build());
        Job job2 = jobRepository.save(Job.builder().name("j2").maxRetry(3).isEnabled(true).build());

        jobQueue.offer(job1);
        jobQueue.offer(job2);

        dispatcherService.dispatchJobExecution();

        assertEquals(1, jobExecutionRepository.count());
    }

    // 4️⃣ Dispatcher should call JobExecutionService
    @Test
    @Transactional
    void dispatcher_should_call_jobExecutionService() {

        Job job = jobRepository.save(
                Job.builder().name("call-test").maxRetry(3).isEnabled(true).build()
        );

        jobQueue.offer(job);

        dispatcherService.dispatchJobExecution();

        List<JobExecution> executions = jobExecutionRepository.findAll();
        assertEquals(1, executions.size());
        assertEquals("call-test", executions.get(0).getJob().getName());
    }

    // 5️⃣ Dispatcher config check
    @Test
    void dispatcher_should_not_start_when_disabled() {
        assertNotEquals(
                com.hilal.Chronos_Scheduler.service.impl.DispatcherServiceImpl.class,
                dispatcherService.getClass(),
                "Real dispatcher must NOT load in test"
        );
    }
}
