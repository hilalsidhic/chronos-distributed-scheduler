package com.hilal.Chronos_Worker.services.Impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hilal.Chronos_Worker.entities.JobExecution;
import com.hilal.Chronos_Worker.entities.dtos.WorkerPayload;
import com.hilal.Chronos_Worker.exceptions.types.NotFoundException;
import com.hilal.Chronos_Worker.repositories.JobExecutionRepository;
import com.hilal.Chronos_Worker.services.ExecutorService;
import com.hilal.Chronos_Worker.services.WorkerDispatcherService;
import io.micrometer.core.instrument.Counter;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionException;

@Service
public class WorkerDispatcherServiceImpl implements WorkerDispatcherService {

    @Autowired
    private BlockingQueue<JobExecution> workerQueue;

    @Autowired
    private ExecutorService executorService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JobExecutionRepository jobExecutionRepository;

    @Autowired
    private Counter workerJobDispatches;

    private volatile boolean running = true;

    @Override
    @EventListener(ApplicationReadyEvent.class)
    public void dispatchJobs() {

        while (running && !Thread.currentThread().isInterrupted()) {

            try {
                // Block until job is available
                JobExecution jobExecution = workerQueue.take();

                if (jobExecution == null) {
                    continue; // safety
                }

                // Convert payload safely
                WorkerPayload workerPayload =
                        objectMapper.convertValue(jobExecution.getPayload(), WorkerPayload.class);

                // Mark dispatch in DB
                startJobDispatch(jobExecution);

                workerJobDispatches.increment();

                // Submit to executor pool
                try {
                    executorService.executeJob(jobExecution, workerPayload);
                } catch (RejectedExecutionException rex) {
                    // BACKPRESSURE: Put job back into queue
                    workerQueue.offer(jobExecution);
                    Thread.sleep(200);
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception ex) {
                // LOG BUT CONTINUE
                System.err.println("Dispatcher error: " + ex.getMessage());
            }
        }
    }

    // Graceful shutdown hook
    @PreDestroy
    public void stopDispatcher() {
        running = false;
    }

    @Override
    @Transactional
    public void startJobDispatch(JobExecution jobExecution) {
        if (jobExecution == null) {
            throw new NotFoundException("JobExecution not found");
        }

        String existing = jobExecution.getLog() == null ? "" : jobExecution.getLog();

        jobExecution.setLog(existing + "\nDispatched at " + OffsetDateTime.now());
        jobExecution.setStartedAt(OffsetDateTime.now());
        jobExecution.setLastHeartbeatAt(OffsetDateTime.now());
        jobExecution.setPickedByWorker(true);

        jobExecutionRepository.save(jobExecution);
    }
}
