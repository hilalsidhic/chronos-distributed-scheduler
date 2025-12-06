package com.hilal.Chronos_Worker.unit.service;

import com.hilal.Chronos_Worker.engines.TimeoutEngine;
import com.hilal.Chronos_Worker.engines.WorkerEngine;
import com.hilal.Chronos_Worker.entities.dtos.RunningJobContext;
import com.hilal.Chronos_Worker.services.Impl.TimeoutServiceImpl;
import org.junit.jupiter.api.*;
import org.mockito.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import static org.mockito.Mockito.*;

class TimeoutServiceImplTest {

    @Mock
    private TimeoutEngine timeoutEngine;

    @Mock
    private WorkerEngine workerEngine;

    @Mock
    private Future<?> future;

    @InjectMocks
    private TimeoutServiceImpl timeoutService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // =====================================================================================
    // fetchTimedOutJobExecution_DB()
    // =====================================================================================

    @Test
    @DisplayName("fetchTimedOutJobExecution_DB should return immediately when no timed out IDs")
    void fetchTimedOutJobExecution_DB_noIds() {
        when(timeoutEngine.fetchTimedOutJobExecutions())
                .thenReturn(List.of());

        timeoutService.fetchTimedOutJobExecution_DB();

        verify(workerEngine, never()).getRunningJobContext(any());
        verify(timeoutEngine, never()).markJobAsTimedOutIfStillRunning(any());
    }

    @Test
    @DisplayName("fetchTimedOutJobExecution_DB should cancel future and mark completed when ctx & future exist")
    void fetchTimedOutJobExecution_DB_cancelAndMarkCompleted() {
        Long id = 10L;

        RunningJobContext ctx = new RunningJobContext();
        ctx.setExecutionId(id);
        ctx.setFuture(future);

        when(timeoutEngine.fetchTimedOutJobExecutions()).thenReturn(List.of(id));
        when(workerEngine.getRunningJobContext(id)).thenReturn(ctx);

        timeoutService.fetchTimedOutJobExecution_DB();

        verify(future).cancel(true);
        verify(workerEngine).markJobAsCompleted(id);
        verify(timeoutEngine).markJobAsTimedOutIfStillRunning(id);
    }

    @Test
    @DisplayName("fetchTimedOutJobExecution_DB should skip cancel when ctx is null")
    void fetchTimedOutJobExecution_DB_ctxNull() {
        Long id = 12L;

        when(timeoutEngine.fetchTimedOutJobExecutions()).thenReturn(List.of(id));
        when(workerEngine.getRunningJobContext(id)).thenReturn(null);

        timeoutService.fetchTimedOutJobExecution_DB();

        verify(future, never()).cancel(anyBoolean());
        verify(workerEngine, never()).markJobAsCompleted(any());
        verify(timeoutEngine).markJobAsTimedOutIfStillRunning(id);
    }

    @Test
    @DisplayName("fetchTimedOutJobExecution_DB should skip cancel when future is null but still mark timed out")
    void fetchTimedOutJobExecution_DB_futureNull() {
        Long id = 15L;

        RunningJobContext ctx = new RunningJobContext();
        ctx.setExecutionId(id);
        ctx.setFuture(null);

        when(timeoutEngine.fetchTimedOutJobExecutions()).thenReturn(List.of(id));
        when(workerEngine.getRunningJobContext(id)).thenReturn(ctx);

        timeoutService.fetchTimedOutJobExecution_DB();

        verify(future, never()).cancel(anyBoolean());
        verify(workerEngine, never()).markJobAsCompleted(any());
        verify(timeoutEngine).markJobAsTimedOutIfStillRunning(id);
    }

    // =====================================================================================
    // fetchTimedOutJobExecution_local()
    // =====================================================================================

    @Test
    @DisplayName("fetchTimedOutJobExecution_local should return when no running jobs")
    void fetchTimedOutJobExecution_local_noRunningJobs() {
        when(workerEngine.getRunningJobs()).thenReturn(Map.of());

        timeoutService.fetchTimedOutJobExecution_local();

        verify(timeoutEngine, never()).markJobAsTimedOutIfStillRunning(any());
    }

    @Test
    @DisplayName("fetchTimedOutJobExecution_local should ignore jobs that have not exceeded time")
    void fetchTimedOutJobExecution_local_notExceeded() {
        RunningJobContext ctx = new RunningJobContext();
        ctx.setExecutionId(1L);
        ctx.setFuture(future);
        ctx.setMaxExecutionSeconds(100);
        ctx.setStartedAt(OffsetDateTime.now().plusSeconds(50)); // in the future → not exceeded

        when(workerEngine.getRunningJobs()).thenReturn(Map.of(1L, ctx));

        timeoutService.fetchTimedOutJobExecution_local();

        verify(future, never()).cancel(anyBoolean());
        verify(timeoutEngine, never()).markJobAsTimedOutIfStillRunning(any());
    }

    @Test
    @DisplayName("fetchTimedOutJobExecution_local should cancel and mark timed out when exceeded")
    void fetchTimedOutJobExecution_local_exceeded() {
        RunningJobContext ctx = new RunningJobContext();
        ctx.setExecutionId(2L);
        ctx.setFuture(future);
        ctx.setMaxExecutionSeconds(1);
        ctx.setStartedAt(OffsetDateTime.now().minusSeconds(10)); // exceeded

        when(workerEngine.getRunningJobs()).thenReturn(Map.of(2L, ctx));
        when(timeoutEngine.markJobAsTimedOutIfStillRunning(2L)).thenReturn(1);

        timeoutService.fetchTimedOutJobExecution_local();

        verify(future).cancel(true);
        verify(timeoutEngine).markJobAsTimedOutIfStillRunning(2L);
        verify(workerEngine).markJobAsCompleted(2L);
    }

    @Test
    @DisplayName("fetchTimedOutJobExecution_local should not mark completed when updated=0")
    void fetchTimedOutJobExecution_local_notUpdated() {
        RunningJobContext ctx = new RunningJobContext();
        ctx.setExecutionId(3L);
        ctx.setFuture(future);
        ctx.setMaxExecutionSeconds(1);
        ctx.setStartedAt(OffsetDateTime.now().minusSeconds(10)); // exceeded

        when(workerEngine.getRunningJobs()).thenReturn(Map.of(3L, ctx));
        when(timeoutEngine.markJobAsTimedOutIfStillRunning(3L)).thenReturn(0);

        timeoutService.fetchTimedOutJobExecution_local();

        verify(workerEngine, never()).markJobAsCompleted(any());
    }
}
