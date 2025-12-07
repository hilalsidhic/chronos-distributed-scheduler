package com.hilal.Chronos_Worker.engines;

import com.hilal.Chronos_Worker.entities.JobExecution;
import com.hilal.Chronos_Worker.entities.dtos.RunningJobContext;
import com.hilal.Chronos_Worker.entities.enums.ExecutionStatus;
import com.hilal.Chronos_Worker.repositories.JobExecutionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.Future;

@Component
public class HeartbeatEngine {
    @Autowired
    private JobExecutionRepository jobExecutionRepository;

    @Transactional
    public void sendHeartbeat(RunningJobContext context) {
        if (context == null) {
            return; // safe exit
        }
        jobExecutionRepository.updateHeartbeatTimestamp(context.getExecutionId());
    }

    @Transactional
    public List<Long> getStuckHeartbeats(int limit) {
        return jobExecutionRepository.findStuckJobExecutionIds(limit);
    }

    @Transactional
    public int markJobAsStuckIfStillRunning(Long id) {
        return jobExecutionRepository.markStuckIfStillRunning(id);
    }

    public void handleStuckHeartbeat(RunningJobContext ctx) {
        Future<?> future = ctx.getFuture();
        if (future != null && !future.isDone()) {
            future.cancel(true);
        }
    }

}
