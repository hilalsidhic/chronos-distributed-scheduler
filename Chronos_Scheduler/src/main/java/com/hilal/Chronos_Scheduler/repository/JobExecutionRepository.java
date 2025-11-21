package com.hilal.Chronos_Scheduler.repository;

import com.hilal.Chronos_Scheduler.entities.JobExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobExecutionRepository extends JpaRepository<JobExecution, Long> {
    @Query(
            value = "SELECT * FROM job_executions " +
                    "WHERE status = 'FAILED' " +
                    "FOR UPDATE SKIP LOCKED " +
                    "LIMIT :limit",
            nativeQuery = true
    )
    List<JobExecution> lockFailedJobExecutions(int limit);

    @Query(
            value = "SELECT * FROM job_executions " +
                    "WHERE status = 'TIMED_OUT' " +
                    "FOR UPDATE SKIP LOCKED " +
                    "LIMIT :limit",
            nativeQuery = true
    )
    List<JobExecution> lockTimedOutJobExecutions(int limit);

    @Query(
            value = "SELECT * FROM job_executions " +
                    "WHERE status = 'SUCCESS' " +
                    "FOR UPDATE SKIP LOCKED " +
                    "LIMIT :limit",
            nativeQuery = true
    )
    List<JobExecution> lockCompletedJobExecutions(int limit);
}
