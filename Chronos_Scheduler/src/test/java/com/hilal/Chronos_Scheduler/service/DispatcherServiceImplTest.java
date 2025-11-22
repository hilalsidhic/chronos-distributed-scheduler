package com.hilal.Chronos_Scheduler.service;

import com.hilal.Chronos_Scheduler.entities.Job;
import com.hilal.Chronos_Scheduler.service.impl.DispatcherServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.concurrent.BlockingQueue;

import static org.mockito.Mockito.*;

class DispatcherServiceImplTest {

    @Mock
    private BlockingQueue<Job> jobQueue;

    @Mock
    private JobExecutionService jobExecutionService;

    @InjectMocks
    private DispatcherServiceImpl dispatcherService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // ----------------------------------------------------------------------

    @Test
    @DisplayName("dispatchJobExecution should take a job and create JobExecution")
    void dispatchJobExecution_createsJobExecution() throws Exception {
        Job job = new Job();

        // First call returns job, second call throws InterruptedException (to break loop)
        when(jobQueue.take()).thenReturn(job).thenThrow(new InterruptedException());

        Thread thread = new Thread(() -> dispatcherService.dispatchJobExecution());
        thread.start();

        // Allow enough time for first take() to run
        Thread.sleep(50);

        // Interrupt to force exit
        thread.interrupt();
        thread.join(200);

        verify(jobExecutionService).createJobExecution(job);
    }

    // ----------------------------------------------------------------------

    @Test
    @DisplayName("dispatchJobExecution exits immediately when take() throws InterruptedException")
    void dispatchJobExecution_exitsOnInterruptedException() throws Exception {

        when(jobQueue.take()).thenThrow(new InterruptedException());

        Thread thread = new Thread(() -> dispatcherService.dispatchJobExecution());
        thread.start();

        Thread.sleep(50);

        thread.interrupt();
        thread.join(200);

        verify(jobExecutionService, never()).createJobExecution(any());
    }
}
