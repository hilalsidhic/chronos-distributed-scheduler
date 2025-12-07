package com.hilal.Chronos_Worker.unit.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hilal.Chronos_Worker.entities.JobExecution;
import com.hilal.Chronos_Worker.entities.dtos.WorkerPayload;
import com.hilal.Chronos_Worker.exceptions.types.NotFoundException;
import com.hilal.Chronos_Worker.repositories.JobExecutionRepository;
import com.hilal.Chronos_Worker.services.ExecutorService;
import com.hilal.Chronos_Worker.services.Impl.WorkerDispatcherServiceImpl;
import org.junit.jupiter.api.*;
import org.mockito.*;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import static org.mockito.Mockito.*;

class WorkerDispatcherServiceImplTest {

    @Mock
    private BlockingQueue<JobExecution> workerQueue;

    @Mock
    private ExecutorService executorService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private JobExecutionRepository jobExecutionRepository;

    @InjectMocks
    private WorkerDispatcherServiceImpl workerDispatcher;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    // --------------------------------------------------------
    // startJobDispatch Tests
    // --------------------------------------------------------

    @Test
    @DisplayName("startJobDispatch should update jobExecution fields and save it")
    void startJobDispatch_updatesFieldsAndSaves() {
        JobExecution je = new JobExecution();
        je.setLog("initial");

        workerDispatcher.startJobDispatch(je);

        Assertions.assertTrue(je.isPickedByWorker());
        Assertions.assertNotNull(je.getStartedAt());
        Assertions.assertNotNull(je.getLastHeartbeatAt());
        Assertions.assertTrue(je.getLog().contains("initial"));
        Assertions.assertTrue(je.getLog().contains("JobExecution taken from worker queue"));

        verify(jobExecutionRepository).save(je);
    }

    @Test
    @DisplayName("startJobDispatch should throw NotFoundException when jobExecution is null")
    void startJobDispatch_throwsWhenNull() {
        Assertions.assertThrows(
                NotFoundException.class,
                () -> workerDispatcher.startJobDispatch(null)
        );

        verify(jobExecutionRepository, never()).save(any());
    }

    // --------------------------------------------------------
    // dispatchJobs Loop Test
    // --------------------------------------------------------

    @Test
    @DisplayName("dispatchJobs should take from queue, convert payload and execute job")
    void dispatchJobs_processesSingleJobThenStopsOnInterrupt() throws Exception {
        // Given a JobExecution in queue
        JobExecution je = new JobExecution();
        je.setPayload(Map.of("key", "value"));

        WorkerPayload wp = new WorkerPayload();

        // First call: return jobExecution
        when(workerQueue.take())
                .thenReturn(je)
                .thenThrow(new InterruptedException()); // stops loop after 1 cycle

        when(objectMapper.convertValue(je.getPayload(), WorkerPayload.class))
                .thenReturn(wp);

        // Execute loop
        workerDispatcher.dispatchJobs();

        // Verify the sequence
        verify(workerQueue, times(2)).take(); // take once + interrupt-trigger
        verify(objectMapper).convertValue(je.getPayload(), WorkerPayload.class);
        verify(executorService).executeJob(je, wp);
    }

    @Test
    @DisplayName("dispatchJobs should stop immediately when interrupted before take")
    void dispatchJobs_stopsImmediatelyOnInterrupt() throws Exception {
        when(workerQueue.take())
                .thenThrow(new InterruptedException());

        workerDispatcher.dispatchJobs();

        verify(executorService, never()).executeJob(any(), any());
    }
}
