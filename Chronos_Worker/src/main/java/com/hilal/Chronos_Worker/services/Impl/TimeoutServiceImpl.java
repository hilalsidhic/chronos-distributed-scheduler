package com.hilal.Chronos_Worker.services.Impl;

import com.hilal.Chronos_Worker.engines.HeartbeatEngine;
import com.hilal.Chronos_Worker.engines.TimeoutEngine;
import com.hilal.Chronos_Worker.engines.WorkerEngine;
import com.hilal.Chronos_Worker.entities.dtos.RunningJobContext;
import com.hilal.Chronos_Worker.services.TimeoutService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Service
public class TimeoutServiceImpl implements TimeoutService {

    @Autowired
    private TimeoutEngine timeoutEngine;

    @Autowired
    private WorkerEngine workerEngine;

    @Value("${worker.max.executions.timedout:10}")
    private int maxExecutionsInTimedOut;

    @Scheduled(fixedRate = 60000)
    @Override
    public void fetchTimedOutJobExecution_DB() {
        List<Long> timedOutJobExecutionIds = timeoutEngine.fetchTimedOutJobExecutions(maxExecutionsInTimedOut);
        if (timedOutJobExecutionIds.isEmpty()) {
            return;
        }
        for (Long jobExecutionId : timedOutJobExecutionIds) {
            RunningJobContext ctx = workerEngine.getRunningJobContext(jobExecutionId);
            if (ctx != null && ctx.getFuture() != null) {
                ctx.getFuture().cancel(true);
                workerEngine.markJobAsCompleted(jobExecutionId);
            }
            timeoutEngine.markJobAsTimedOutIfStillRunning(jobExecutionId);
        }
        return;
    }

    @Scheduled(fixedRate = 5000)
    @Override
    public void fetchTimedOutJobExecution_local() {
        Map<Long, RunningJobContext> running = workerEngine.getRunningJobs();
        if (running.isEmpty()) {
            return;
        }
        OffsetDateTime now = OffsetDateTime.now();
        for (RunningJobContext ctx : running.values()) {
            boolean exceeded = now.isAfter(
                    ctx.getStartedAt().plusSeconds(ctx.getMaxExecutionSeconds())
            );
            if (exceeded) {
                if (ctx.getFuture() != null) {
                    ctx.getFuture().cancel(true);
                }
                int updated = timeoutEngine.markJobAsTimedOutIfStillRunning(ctx.getExecutionId());
                if (updated > 0)
                workerEngine.markJobAsCompleted(ctx.getExecutionId());
            }
        }
    }

}
