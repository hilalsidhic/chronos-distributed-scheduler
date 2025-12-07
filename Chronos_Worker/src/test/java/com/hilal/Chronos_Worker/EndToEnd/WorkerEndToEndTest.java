package com.hilal.Chronos_Worker.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hilal.Chronos_Worker.config.AbstractPostgresTest;
import com.hilal.Chronos_Worker.entities.JobExecution;
import com.hilal.Chronos_Worker.entities.dtos.RunningJobContext;
import com.hilal.Chronos_Worker.entities.enums.ExecutionStatus;
import com.hilal.Chronos_Worker.entities.dtos.HttpResult;
import com.hilal.Chronos_Worker.entities.dtos.WorkerPayload;
import com.hilal.Chronos_Worker.engines.HttpEngine;
import com.hilal.Chronos_Worker.engines.TimeoutEngine;
import com.hilal.Chronos_Worker.engines.HeartbeatEngine;
import com.hilal.Chronos_Worker.engines.WorkerEngine;
import com.hilal.Chronos_Worker.repositories.JobExecutionRepository;
import com.hilal.Chronos_Worker.services.Impl.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
class WorkerEndToEndTest extends AbstractPostgresTest {

    @Autowired private JobExecutionRepository repo;
    @Autowired private ObjectMapper mapper;

    // -------------------------------------------------------------------------
    // MOCK BEAN: Silences the real Spring Bean to prevent @EventListener loop
    // -------------------------------------------------------------------------
    @MockBean
    private WorkerDispatcherServiceImpl silencedSpringBean;

    // -------------------------------------------------------------------------
    // MANUAL INSTANCES: For testing logic without the loop
    // -------------------------------------------------------------------------
    private WorkerDispatcherServiceImpl dispatcherUnderTest; // Manual instance
    private WorkerSchedulerServiceImpl scheduler;
    private ExecutorServiceImpl executor;
    private TimeoutServiceImpl timeoutService;
    private HeartbeatServiceImpl heartbeatService;

    // Engines & Mocks
    private HttpEngine httpEngine;
    private WorkerEngine workerEngine;
    private TimeoutEngine timeoutEngine;
    private HeartbeatEngine heartbeatEngine;

    private ArrayBlockingQueue<JobExecution> queue;

    @BeforeEach
    void setup() {
        queue = new ArrayBlockingQueue<>(10);

        // Mocks
        httpEngine = mock(HttpEngine.class);
        workerEngine = spy(new WorkerEngine());
        timeoutEngine = mock(TimeoutEngine.class);
        heartbeatEngine = mock(HeartbeatEngine.class);

        // --- Executor Service ---
        executor = new ExecutorServiceImpl();
        ReflectionTestUtils.setField(executor, "httpEngine", httpEngine);
        ReflectionTestUtils.setField(executor, "workerEngine", workerEngine);
        ReflectionTestUtils.setField(executor, "jobExecutionRepository", repo);
        // Use Synchronous Executor for tests
        ReflectionTestUtils.setField(executor, "threadPoolTaskExecutor", new DirectExecutor());

        // --- Dispatcher Service (Manual Instance) ---
        dispatcherUnderTest = new WorkerDispatcherServiceImpl();
        ReflectionTestUtils.setField(dispatcherUnderTest, "workerQueue", queue);
        ReflectionTestUtils.setField(dispatcherUnderTest, "executorService", executor);
        ReflectionTestUtils.setField(dispatcherUnderTest, "objectMapper", mapper);
        ReflectionTestUtils.setField(dispatcherUnderTest, "jobExecutionRepository", repo);

        // --- Scheduler Service ---
        scheduler = new WorkerSchedulerServiceImpl();
        ReflectionTestUtils.setField(scheduler, "workerQueue", queue);
        ReflectionTestUtils.setField(scheduler, "workerEngine", workerEngine);
        ReflectionTestUtils.setField(scheduler, "jobExecutionRepository", repo);

        // --- Timeout Service ---
        timeoutService = new TimeoutServiceImpl();
        ReflectionTestUtils.setField(timeoutService, "timeoutEngine", timeoutEngine);
        ReflectionTestUtils.setField(timeoutService, "workerEngine", workerEngine);

        // --- Heartbeat Service ---
        heartbeatService = new HeartbeatServiceImpl();
        ReflectionTestUtils.setField(heartbeatService, "heartbeatEngine", heartbeatEngine);
        ReflectionTestUtils.setField(heartbeatService, "workerEngine", workerEngine);
    }

    // ============================================================
    // 1) SUCCESS FLOW
    // ============================================================
    @Test
    void endToEnd_successfulExecution() throws Exception {
        // 1. Setup Data
        JobExecution je = createPending();
        when(httpEngine.execute(any())).thenReturn(new HttpResult(200, "OK", 0L, true, "OK"));

        // 2. Scheduler: Fetch PENDING -> Set RUNNING -> Push to Queue
        scheduler.fetchAndExecuteJobs();

        // 3. Dispatcher: Poll Queue -> Start Dispatch -> Execute
        JobExecution taken = queue.poll(1, TimeUnit.SECONDS);
        assertThat(taken).isNotNull();

        dispatcherUnderTest.startJobDispatch(taken);
        executor.executeJob(taken, mapper.convertValue(taken.getPayload(), WorkerPayload.class));

        // 4. Verification
        JobExecution updated = repo.findById(je.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(ExecutionStatus.SUCCESS);
        verify(workerEngine).markJobAsCompleted(je.getId());
    }

    // ============================================================
    // 2) FAILED FLOW
    // ============================================================
    @Test
    void endToEnd_failedExecution() throws Exception {
        JobExecution je = createPending();
        when(httpEngine.execute(any())).thenReturn(new HttpResult(500, "ERR", 0L, false, "ERR"));

        scheduler.fetchAndExecuteJobs();
        JobExecution taken = queue.poll(1, TimeUnit.SECONDS);
        assertThat(taken).isNotNull();

        dispatcherUnderTest.startJobDispatch(taken);
        executor.executeJob(taken, mapper.convertValue(taken.getPayload(), WorkerPayload.class));

        JobExecution updated = repo.findById(je.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(ExecutionStatus.FAILED);
        verify(workerEngine).markJobAsCompleted(je.getId());
    }

    // ============================================================
    // 3) DB STATUS CHANGES MID-EXECUTION (Race Condition Check)
    // ============================================================
    @Test
    void endToEnd_dbStatusChanges_midExecution_shouldSkipExecutorUpdate() throws Exception {
        JobExecution je = createPending();

        when(httpEngine.execute(any())).then(inv -> {
            // Simulate: Another node/thread updated status to SUCCESS while HTTP call was happening
            je.setStatus(ExecutionStatus.SUCCESS);
            repo.saveAndFlush(je);
            return new HttpResult(200, "OK", 0L, true, "OK");
        });

        scheduler.fetchAndExecuteJobs();
        JobExecution taken = queue.poll(1, TimeUnit.SECONDS);
        assertThat(taken).isNotNull();

        dispatcherUnderTest.startJobDispatch(taken);
        executor.executeJob(taken, mapper.convertValue(taken.getPayload(), WorkerPayload.class));

        JobExecution updated = repo.findById(je.getId()).orElseThrow();
        // It should remain SUCCESS (from the mid-run update), not overwritten by Executor
        assertThat(updated.getStatus()).isEqualTo(ExecutionStatus.SUCCESS);

        verify(workerEngine).markJobAsCompleted(je.getId());
    }

    // ============================================================
    // 4) TIMEOUT LOGIC
    // ============================================================
    @Test
    void endToEnd_timeout_logic() {
        // Setup a running job
        JobExecution je = repo.saveAndFlush(
                JobExecution.builder()
                        .status(ExecutionStatus.RUNNING)
                        .startedAt(OffsetDateTime.now().minusSeconds(100))
                        .maxExecutionTime(10)
                        .build()
        );

        RunningJobContext ctx = RunningJobContext.builder()
                .executionId(je.getId())
                .startedAt(je.getStartedAt())
                .maxExecutionSeconds(je.getMaxExecutionTime())
                .future(mock(Future.class)) // Mock future
                .build();

        // Simulate WorkerEngine knowing about this job
        when(workerEngine.getRunningJobs()).thenReturn(Map.of(je.getId(), ctx));

        // Mock TimeoutEngine to simulate DB update behavior
        when(timeoutEngine.markJobAsTimedOutIfStillRunning(je.getId())).thenAnswer(inv -> {
            je.setStatus(ExecutionStatus.TIMED_OUT);
            repo.saveAndFlush(je);
            return 1;
        });

        // Execute Logic
        timeoutService.fetchTimedOutJobExecution_local();

        // Verify
        JobExecution updated = repo.findById(je.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(ExecutionStatus.TIMED_OUT);
        verify(ctx.getFuture()).cancel(true); // Ensure task was cancelled
    }

    // ============================================================
    // 5) HEARTBEAT LOGIC
    // ============================================================
    @Test
    void endToEnd_heartbeatLogic() {
        JobExecution je = repo.saveAndFlush(
                JobExecution.builder()
                        .status(ExecutionStatus.RUNNING)
                        .startedAt(OffsetDateTime.now())
                        .lastHeartbeatAt(OffsetDateTime.now().minusSeconds(20))
                        .build()
        );

        RunningJobContext ctx = RunningJobContext.builder()
                .executionId(je.getId())
                .jobExecution(je)
                .build();

        when(workerEngine.getRunningJobs()).thenReturn(Map.of(je.getId(), ctx));

        heartbeatService.getJobsAndSendHeartbeat();

        // Verify Engine was called to update DB
        verify(heartbeatEngine).sendHeartbeat(any());
    }

    // ============================================================
    // HELPER CLASSES & METHODS
    // ============================================================

    private JobExecution createPending() {
        return repo.saveAndFlush(JobExecution.builder()
                .status(ExecutionStatus.PENDING)
                .payload(Map.of("url", "http://example.com"))
                .maxExecutionTime(30)
                .createdAt(OffsetDateTime.now())
                .build());
    }

    /**
     * A synchronous executor.
     * Unlike typical ThreadPools, this runs the task IMMEDIATELY on the same thread.
     * This eliminates the need for Thread.sleep() or Future.get() in tests.
     */
    static class DirectExecutor extends ThreadPoolTaskExecutor {
        @Override
        public Future<?> submit(Runnable task) {
            FutureTask<?> f = new FutureTask<>(task, null);
            f.run(); // Run synchronously
            return f;
        }
    }
}