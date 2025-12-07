package com.hilal.Chronos_Worker.unit.service;

import com.hilal.Chronos_Worker.engines.HeartbeatEngine;
import com.hilal.Chronos_Worker.engines.WorkerEngine;
import com.hilal.Chronos_Worker.entities.dtos.RunningJobContext;
import com.hilal.Chronos_Worker.services.Impl.HeartbeatServiceImpl;
import org.junit.jupiter.api.*;
import org.mockito.*;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;

class HeartbeatServiceImplTest {

    @Mock
    private HeartbeatEngine heartbeatEngine;

    @Mock
    private WorkerEngine workerEngine;

    @InjectMocks
    private HeartbeatServiceImpl heartbeatService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // ============================================================================
    // getJobsAndSendHeartbeat()
    // ============================================================================

    @Test
    @DisplayName("getJobsAndSendHeartbeat should return when no running jobs")
    void getJobsAndSendHeartbeat_noRunningJobs() {
        when(workerEngine.getRunningJobs()).thenReturn(Map.of());

        heartbeatService.getJobsAndSendHeartbeat();

        verify(heartbeatEngine, never()).sendHeartbeat(any());
    }

    @Test
    @DisplayName("getJobsAndSendHeartbeat should send heartbeat for each running job")
    void getJobsAndSendHeartbeat_sendsHeartbeats() {
        RunningJobContext ctx1 = new RunningJobContext();
        ctx1.setExecutionId(1L);

        RunningJobContext ctx2 = new RunningJobContext();
        ctx2.setExecutionId(2L);

        Map<Long, RunningJobContext> running = Map.of(
                1L, ctx1,
                2L, ctx2
        );

        when(workerEngine.getRunningJobs()).thenReturn(running);

        heartbeatService.getJobsAndSendHeartbeat();

        verify(heartbeatEngine).sendHeartbeat(ctx1);
        verify(heartbeatEngine).sendHeartbeat(ctx2);
        verify(heartbeatEngine, times(2)).sendHeartbeat(any());
    }

    // ============================================================================
    // cleanUpStuckHeartbeats()
    // ============================================================================

    @Test
    @DisplayName("cleanUpStuckHeartbeats should return when no stuck heartbeats")
    void cleanUpStuckHeartbeats_noStuckIds() {
        when(heartbeatEngine.getStuckHeartbeats()).thenReturn(List.of());

        heartbeatService.cleanUpStuckHeartbeats();

        verify(workerEngine, never()).markJobAsCompleted(any());
        verify(heartbeatEngine, never()).markJobAsStuckIfStillRunning(any());
    }

    @Test
    @DisplayName("cleanUpStuckHeartbeats should handle stuck heartbeat when context exists")
    void cleanUpStuckHeartbeats_ctxExists() {
        Long id = 100L;

        RunningJobContext ctx = new RunningJobContext();
        ctx.setExecutionId(id);

        when(heartbeatEngine.getStuckHeartbeats()).thenReturn(List.of(id));
        when(workerEngine.getRunningJobContext(id)).thenReturn(ctx);

        heartbeatService.cleanUpStuckHeartbeats();

        verify(heartbeatEngine).handleStuckHeartbeat(ctx);
        verify(workerEngine).markJobAsCompleted(id);
        verify(heartbeatEngine).markJobAsStuckIfStillRunning(id);
    }

    @Test
    @DisplayName("cleanUpStuckHeartbeats should skip handleStuckHeartbeat when ctx is null")
    void cleanUpStuckHeartbeats_ctxNull() {
        Long id = 200L;

        when(heartbeatEngine.getStuckHeartbeats()).thenReturn(List.of(id));
        when(workerEngine.getRunningJobContext(id)).thenReturn(null);

        heartbeatService.cleanUpStuckHeartbeats();

        verify(heartbeatEngine, never()).handleStuckHeartbeat(any());
        verify(workerEngine).markJobAsCompleted(id);
        verify(heartbeatEngine).markJobAsStuckIfStillRunning(id);
    }
}
