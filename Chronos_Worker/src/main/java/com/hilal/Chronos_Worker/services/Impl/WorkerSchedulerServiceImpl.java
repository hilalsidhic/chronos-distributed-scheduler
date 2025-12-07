package com.hilal.Chronos_Worker.services.Impl;

import com.hilal.Chronos_Worker.engines.WorkerEngine;
import com.hilal.Chronos_Worker.entities.JobExecution;
import com.hilal.Chronos_Worker.entities.dtos.RunningJobContext;
import com.hilal.Chronos_Worker.entities.enums.ExecutionStatus;
import com.hilal.Chronos_Worker.repositories.JobExecutionRepository;
import com.hilal.Chronos_Worker.services.WorkerSchedulerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

@Service
public class WorkerSchedulerServiceImpl implements WorkerSchedulerService {

    @Autowired
    private JobExecutionRepository jobExecutionRepository;

    @Autowired
    private BlockingQueue<JobExecution> workerQueue;

    @Autowired
    private WorkerEngine workerEngine;

    @Value("${worker.max.jobs.process:10}")
    private int maxJobsToProcess;

    @Override
    @Scheduled(fixedRate = 2000)
    public void fetchAndExecuteJobs() {
        List<JobExecution> jobExecutions = getJobsAndSetToRunning();
        if (jobExecutions.isEmpty()) {
            return;
        }
        for (JobExecution jobExecution : jobExecutions) {
            jobExecution.setLog(
                    jobExecution.getLog() + "\nJobExecution added to worker queue at " + OffsetDateTime.now()
            );
            try {
                boolean offered = workerQueue.offer(jobExecution, 1000, TimeUnit.MILLISECONDS);
                if (!offered) {
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    @Override
    @Transactional
    public List<JobExecution> getJobsAndSetToRunning() {
        List<JobExecution> jobExecutions = jobExecutionRepository.lockPendingJobExecutions(maxJobsToProcess);
        if (jobExecutions.isEmpty()) {
            return jobExecutions;
        }
        for (JobExecution jobExecution : jobExecutions) {
            jobExecution.setStatus(ExecutionStatus.RUNNING);
            String existingLog = jobExecution.getLog() == null ? "" : jobExecution.getLog();
            jobExecution.setLog(existingLog + "\nJobExecution set to RUNNING at " + OffsetDateTime.now().toString());
        }
        jobExecutionRepository.saveAll(jobExecutions);
        return jobExecutions;
    }

    @Override
    @Scheduled(fixedRate = 8000)
    public void reconcileWorkerState() {
        Map<Long, RunningJobContext> running = workerEngine.getRunningJobs();
        if (running.isEmpty()) {
            return;
        }
        for (Long id : running.keySet()) {
            ExecutionStatus dbStatus = jobExecutionRepository.findStatusById(id);
            if (dbStatus != ExecutionStatus.RUNNING) {
                RunningJobContext ctx = workerEngine.getRunningJobContext(id);
                if (ctx == null) {
                    workerEngine.markJobAsCompleted(id);
                    continue;
                }
                if (ctx != null && ctx.getFuture() != null && !ctx.getFuture().isDone()) {
                    ctx.getFuture().cancel(true);
                }
                workerEngine.markJobAsCompleted(id);
            }
        }
    }

}
