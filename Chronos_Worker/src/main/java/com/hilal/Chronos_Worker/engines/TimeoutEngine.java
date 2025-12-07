package com.hilal.Chronos_Worker.engines;

import com.hilal.Chronos_Worker.repositories.JobExecutionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class TimeoutEngine {

    @Autowired
    private JobExecutionRepository jobExecutionRepository;

    public List<Long> fetchTimedOutJobExecutions(int limit) {
        return jobExecutionRepository.findTimedOutJobExecutionIds(limit);
    }

    @Transactional
    public int markJobAsTimedOutIfStillRunning(Long id) {
        return jobExecutionRepository.markTimedOutIfStillRunning(id);
    }
}
