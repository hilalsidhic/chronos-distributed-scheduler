package com.hilal.Chronos_Worker.services.Impl;

import com.hilal.Chronos_Worker.engines.HeartbeatEngine;
import com.hilal.Chronos_Worker.engines.WorkerEngine;
import com.hilal.Chronos_Worker.entities.JobExecution;
import com.hilal.Chronos_Worker.entities.dtos.RunningJobContext;
import com.hilal.Chronos_Worker.services.HeartbeatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class HeartbeatServiceImpl implements HeartbeatService {
    @Autowired
    private HeartbeatEngine heartbeatEngine;

    @Autowired
    private WorkerEngine workerEngine;

    @Value("${worker.max.executions.stuck:10}")
    private int maxExecutionsInStuck;

    @Scheduled(fixedRate = 5000)
    @Override
    public void getJobsAndSendHeartbeat() {
        Map<Long, RunningJobContext> runningJobs = workerEngine.getRunningJobs();
        if(runningJobs.isEmpty()){
            return;
        }
        for(Long jobExecutionId : runningJobs.keySet()){
            RunningJobContext runningJobContext = runningJobs.get(jobExecutionId);
            if (runningJobContext == null) continue;
            heartbeatEngine.sendHeartbeat(runningJobContext);
        }
        return;
    }

    @Scheduled(fixedRate = 10000)
    @Override
    public void cleanUpStuckHeartbeats() {
        List<Long> stuckJobExecutionIds = heartbeatEngine.getStuckHeartbeats(maxExecutionsInStuck);
        if (stuckJobExecutionIds.isEmpty()) {
            return;
        }
        for (Long id : stuckJobExecutionIds) {
            RunningJobContext ctx = workerEngine.getRunningJobContext(id);
            if (ctx != null) {
                heartbeatEngine.handleStuckHeartbeat(ctx);
            }
            workerEngine.markJobAsCompleted(id);
            int updated = heartbeatEngine.markJobAsStuckIfStillRunning(id);
        }
    }
}
