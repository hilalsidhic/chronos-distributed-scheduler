package com.hilal.Chronos_Scheduler.config;

import com.hilal.Chronos_Scheduler.entities.Job;
import com.hilal.Chronos_Scheduler.repository.JobRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ConcurrencyJobRunner {

    @Autowired
    private JobRepository jobRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<Job> fetchLockedJobs(int limit) {
        return jobRepository.lockPendingJobs(limit);
    }

}
