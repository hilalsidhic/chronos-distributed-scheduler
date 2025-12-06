package com.hilal.Chronos_Worker.services;

public interface TimeoutService {
    void fetchTimedOutJobExecution_DB();
    void fetchTimedOutJobExecution_local();
}
