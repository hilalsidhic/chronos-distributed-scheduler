package com.hilal.Chronos_Worker.unit.service;

import com.hilal.Chronos_Worker.engines.WorkerEngine;
import com.hilal.Chronos_Worker.entities.JobExecution;
import com.hilal.Chronos_Worker.entities.dtos.RunningJobContext;
import com.hilal.Chronos_Worker.entities.enums.ExecutionStatus;
import com.hilal.Chronos_Worker.repositories.JobExecutionRepository;
import com.hilal.Chronos_Worker.services.Impl.WorkerSchedulerServiceImpl;
import org.junit.jupiter.api.*;
import org.mockito.*;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.*;

import static org.mockito.Mockito.*;

class WorkerSchedulerServiceImplTest {

    @Mock
    private JobExecutionRepository jobExecutionRepository;

    @Mock
    private BlockingQueue<JobExecution> workerQueue;

    @Mock
    private WorkerEngine workerEngine;

    @InjectMocks
    private WorkerSchedulerServiceImpl workerService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    // --------------------------------------------------------------
    // TEST 1: getJobsAndSetToRunning()
    // --------------------------------------------------------------
    @Test
    @DisplayName("getJobsAndSetToRunning should set job executions to RUNNING and save them")
    void getJobsAndSetToRunning_setsStatusAndSaves() {
        JobExecution je = new JobExecution();
        je.setStatus(ExecutionStatus.PENDING);
        je.setLog("");

        List<JobExecution> list = List.of(je);

        when(jobExecutionRepository.lockPendingJobExecutions())
                .thenReturn(list);

        List<JobExecution> result = workerService.getJobsAndSetToRunning();

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(ExecutionStatus.RUNNING, je.getStatus());
        Assertions.assertTrue(je.getLog().contains("RUNNING"));
        verify(jobExecutionRepository).saveAll(list);
    }

    @Test
    @DisplayName("getJobsAndSetToRunning should return empty list if no jobs available")
    void getJobsAndSetToRunning_returnsEmptyList() {
        when(jobExecutionRepository.lockPendingJobExecutions())
                .thenReturn(Collections.emptyList());

        List<JobExecution> result = workerService.getJobsAndSetToRunning();

        Assertions.assertTrue(result.isEmpty());
        verify(jobExecutionRepository, never()).saveAll(anyList());
    }

    // --------------------------------------------------------------
    // TEST 2: fetchAndExecuteJobs()
    // --------------------------------------------------------------
    @Test
    @DisplayName("fetchAndExecuteJobs should add jobs to queue when available")
    void fetchAndExecuteJobs_addsJobToQueue() throws Exception {
        JobExecution je = new JobExecution();
        je.setLog("");

        when(jobExecutionRepository.lockPendingJobExecutions())
                .thenReturn(List.of(je));

        when(workerQueue.offer(eq(je), anyLong(), any(TimeUnit.class)))
                .thenReturn(true);

        workerService.fetchAndExecuteJobs();

        Assertions.assertTrue(je.getLog().contains("added to worker queue"));
    }

    @Test
    @DisplayName("fetchAndExecuteJobs should do nothing when no job executions exist")
    void fetchAndExecuteJobs_noJobs_noQueueCall() {
        when(jobExecutionRepository.lockPendingJobExecutions())
                .thenReturn(Collections.emptyList());

        workerService.fetchAndExecuteJobs();

        try {
            verify(workerQueue, never()).offer(any(), anyLong(), any());
        } catch (InterruptedException e) {
            // ignore, Mockito never actually throws here
        }

    }

    @Test
    @DisplayName("fetchAndExecuteJobs should stop execution if queue offer throws InterruptedException")
    void fetchAndExecuteJobs_interruptDuringOffer() throws Exception {
        JobExecution je = new JobExecution();
        je.setLog("");

        when(jobExecutionRepository.lockPendingJobExecutions())
                .thenReturn(List.of(je));

        when(workerQueue.offer(any(JobExecution.class), anyLong(), any(TimeUnit.class)))
                .thenThrow(new InterruptedException());

        workerService.fetchAndExecuteJobs();

        verify(workerQueue).offer(any(JobExecution.class), anyLong(), any(TimeUnit.class));
    }


    // --------------------------------------------------------------
    // TEST 3: reconcileWorkerState()
    // --------------------------------------------------------------
    @Test
    @DisplayName("reconcileWorkerState should cancel future when status is no longer RUNNING")
    void reconcileWorkerState_cancelsFutureIfNotRunning() {

        Long id = 1L;

        Future<?> future = mock(Future.class);
        when(future.isDone()).thenReturn(false);

        RunningJobContext ctx = RunningJobContext.builder()
                .executionId(id)
                .future(future)
                .build();

        Map<Long, RunningJobContext> map = Map.of(id, ctx);

        when(workerEngine.getRunningJobs()).thenReturn(map);

        // IMPORTANT FIX — make DB return NOT RUNNING
        when(jobExecutionRepository.findStatusById(id))
                .thenReturn(ExecutionStatus.SUCCESS);

        when(workerEngine.getRunningJobContext(id)).thenReturn(ctx);

        workerService.reconcileWorkerState();

        verify(future).cancel(true);           // Should now pass
        verify(workerEngine).markJobAsCompleted(id);
    }

    @Test
    @DisplayName("reconcileWorkerState should NOT cancel job if DB status is still RUNNING")
    void reconcileWorkerState_doesNotCancelWhenStillRunning() {
        Long id = 20L;

        RunningJobContext ctx = RunningJobContext.builder()
                .executionId(id)
                .future(mock(Future.class))
                .build();

        when(workerEngine.getRunningJobs())
                .thenReturn(Map.of(id, ctx));

        when(jobExecutionRepository.findStatusById(id))
                .thenReturn(ExecutionStatus.RUNNING);

        workerService.reconcileWorkerState();

        verify(ctx.getFuture(), never()).cancel(true);
        verify(workerEngine, never()).markJobAsCompleted(any());
    }

    @Test
    @DisplayName("reconcileWorkerState should return immediately if no running jobs exist")
    void reconcileWorkerState_noRunningJobs() {
        when(workerEngine.getRunningJobs()).thenReturn(Collections.emptyMap());

        workerService.reconcileWorkerState();

        verify(jobExecutionRepository, never()).findStatusById(any());
    }

    @Test
    @DisplayName("reconcileWorkerState should handle null context but still mark job as completed")
    void reconcileWorkerState_nullContext_marksCompleted() {
        Long id = 99L;

        Map<Long, RunningJobContext> map = new HashMap<>();
        map.put(id, null);

        when(workerEngine.getRunningJobs()).thenReturn(map);
        when(jobExecutionRepository.findStatusById(id)).thenReturn(ExecutionStatus.SUCCESS);
        when(workerEngine.getRunningJobContext(id)).thenReturn(null);

        workerService.reconcileWorkerState();

        verify(workerEngine).markJobAsCompleted(id);            // ✔ correct expectation
    }


}
