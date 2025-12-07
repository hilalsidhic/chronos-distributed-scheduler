package com.hilal.Chronos_Worker.unit.service;

import com.hilal.Chronos_Worker.engines.HttpEngine;
import com.hilal.Chronos_Worker.engines.WorkerEngine;
import com.hilal.Chronos_Worker.entities.JobExecution;
import com.hilal.Chronos_Worker.entities.dtos.HttpResult;
import com.hilal.Chronos_Worker.entities.dtos.RunningJobContext;
import com.hilal.Chronos_Worker.entities.dtos.WorkerPayload;
import com.hilal.Chronos_Worker.entities.enums.ExecutionStatus;
import com.hilal.Chronos_Worker.exceptions.types.NotFoundException;
import com.hilal.Chronos_Worker.repositories.JobExecutionRepository;
import com.hilal.Chronos_Worker.services.Impl.ExecutorServiceImpl;

import org.junit.jupiter.api.*;
import org.mockito.*;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.time.OffsetDateTime;
import java.util.concurrent.Future;

import static org.mockito.Mockito.*;

class ExecutorServiceImplTest {

    @Mock
    private JobExecutionRepository jobExecutionRepository;

    @Mock
    private HttpEngine httpEngine;

    @Mock
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Mock
    private WorkerEngine workerEngine;

    @InjectMocks
    private ExecutorServiceImpl executorService;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    // -------------------------------------------------------------------------
    // executeJob()
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("executeJob should throw when jobExecution is null")
    void executeJob_throwsOnNull() {
        WorkerPayload payload = new WorkerPayload();

        Assertions.assertThrows(NotFoundException.class, () ->
                executorService.executeJob(null, payload));
    }

    @Test
    @DisplayName("executeJob should create RunningJobContext and call workerEngine.addRunningJob")
    void executeJob_createsContext() {

        JobExecution je = new JobExecution();
        je.setId(1L);
        je.setStartedAt(OffsetDateTime.now());
        je.setMaxExecutionTime(60);

        WorkerPayload payload = new WorkerPayload();

        Future<?> mockFuture = mock(Future.class);

        // ThreadPoolTaskExecutor submit mock
        when(threadPoolTaskExecutor.submit(any(Runnable.class)))
                .thenAnswer(invocation -> mockFuture);

        executorService.executeJob(je, payload);

        verify(threadPoolTaskExecutor).submit(any(Runnable.class));

        verify(workerEngine).addRunningJob(eq(1L), argThat(ctx ->
                ctx.getExecutionId() == 1L &&
                        ctx.getFuture() == mockFuture &&
                        ctx.getStartedAt().equals(je.getStartedAt()) &&
                        ctx.getMaxExecutionSeconds() == je.getMaxExecutionTime()
        ));
    }

    // -------------------------------------------------------------------------
    // updateJob()
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("updateJob should throw when jobExecution is null")
    void updateJob_throwsOnNull() {
        Assertions.assertThrows(NotFoundException.class, () ->
                executorService.updateJob(null, "log", ExecutionStatus.SUCCESS));
    }

    @Test
    @DisplayName("updateJob should append log, update status and save")
    void updateJob_updatesAndSaves() {

        JobExecution je = new JobExecution();
        je.setLog("initial");

        executorService.updateJob(je, "update text", ExecutionStatus.SUCCESS);

        Assertions.assertTrue(je.getLog().contains("initial"));
        Assertions.assertTrue(je.getLog().contains("update text"));
        Assertions.assertEquals(ExecutionStatus.SUCCESS, je.getStatus());
        Assertions.assertNotNull(je.getFinishedAt());

        verify(jobExecutionRepository).save(je);
    }

    // -------------------------------------------------------------------------
    // runJob()
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("runJob should mark job SUCCESS on 2xx response")
    void runJob_success() {

        JobExecution je = new JobExecution();
        je.setId(10L);
        je.setLog("");

        WorkerPayload payload = new WorkerPayload();

        HttpResult result = new HttpResult(
                200,
                "OK",
                30L,
                true,
                null
        );

        when(httpEngine.execute(payload)).thenReturn(result);
        when(jobExecutionRepository.findStatusById(10L)).thenReturn(ExecutionStatus.RUNNING);

        executorService.runJob(je, payload);

        Assertions.assertEquals(ExecutionStatus.SUCCESS, je.getStatus());
        verify(jobExecutionRepository).save(je);
        verify(workerEngine).markJobAsCompleted(10L);
    }

    @Test
    @DisplayName("runJob should mark job FAILED on non-2xx response")
    void runJob_failure() {

        JobExecution je = new JobExecution();
        je.setId(20L);
        je.setLog("");

        WorkerPayload payload = new WorkerPayload();

        HttpResult result = new HttpResult(
                500,
                "Server Error",
                30L,
                false,
                "Internal error"
        );

        when(httpEngine.execute(payload)).thenReturn(result);
        when(jobExecutionRepository.findStatusById(20L)).thenReturn(ExecutionStatus.RUNNING);

        executorService.runJob(je, payload);

        Assertions.assertEquals(ExecutionStatus.FAILED, je.getStatus());
        verify(jobExecutionRepository).save(je);
        verify(workerEngine).markJobAsCompleted(20L);
    }

    @Test
    @DisplayName("runJob should NOT update job if status != RUNNING")
    void runJob_doesNotUpdateIfStatusChanged() {

        JobExecution je = new JobExecution();
        je.setId(30L);
        je.setLog("");

        WorkerPayload payload = new WorkerPayload();

        HttpResult result = new HttpResult(
                200,
                "OK",
                30L,
                true,
                null
        );

        when(httpEngine.execute(payload)).thenReturn(result);
        when(jobExecutionRepository.findStatusById(30L)).thenReturn(ExecutionStatus.SUCCESS);

        executorService.runJob(je, payload);

        verify(jobExecutionRepository, never()).save(any());
        verify(workerEngine).markJobAsCompleted(30L);
    }

    @Test
    @DisplayName("runJob should always call markJobAsCompleted even when exception occurs")
    void runJob_alwaysCompletes() {

        JobExecution je = new JobExecution();
        je.setId(40L);

        WorkerPayload payload = new WorkerPayload();

        when(httpEngine.execute(payload)).thenThrow(new RuntimeException("boom"));

        try {
            executorService.runJob(je, payload);
        } catch (Exception ignored) {
            // swallow, because method under test is allowed to throw
        }

        verify(workerEngine).markJobAsCompleted(40L);
    }

}
