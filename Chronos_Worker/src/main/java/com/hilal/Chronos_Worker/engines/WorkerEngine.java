package com.hilal.Chronos_Worker.engines;

import com.hilal.Chronos_Worker.entities.dtos.RunningJobContext;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class WorkerEngine {
    private final ConcurrentMap<Long, RunningJobContext> runningJobs = new ConcurrentHashMap<>();

    public void addRunningJob(Long jobId, RunningJobContext context) {
        runningJobs.put(jobId, context);
    }

    public void markJobAsCompleted(Long jobId) {
        runningJobs.remove(jobId);
    }

    public RunningJobContext getRunningJobContext(Long jobId) {
        return runningJobs.get(jobId);
    }

    public Map<Long, RunningJobContext> getRunningJobs() {
        return runningJobs;
    }

}
