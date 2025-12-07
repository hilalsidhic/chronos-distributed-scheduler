package com.hilal.Chronos_Worker.services;

public interface HeartbeatService {
    public void getJobsAndSendHeartbeat();
    public void cleanUpStuckHeartbeats();
}
