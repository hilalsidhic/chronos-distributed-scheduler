package com.hilal.Chronos_Scheduler.repository;

import com.hilal.Chronos_Scheduler.entities.Job;
import com.hilal.Chronos_Scheduler.service.JobService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {
    @Query(
            value = "SELECT * FROM jobs " +
                    "WHERE next_execution_time<=NOW() " +
                    "AND status = 'PENDING' " +
                    "AND retry_count < max_retry " +
                    "AND is_enabled = True " +
                    "FOR UPDATE SKIP LOCKED " +
                    "ORDER BY next_execution_time ASC "+
                    "LIMIT :limit",
            nativeQuery = true
    )
    List<Job> lockPendingJobs(int limit);


    @Query(
            value = "SELECT * FROM jobs " +
                    "WHERE status= 'RESERVED' " +
                    "AND reserved_at <= NOW() - INTERVAL '60 seconds' "+
                    "FOR UPDATE SKIP LOCKED ",
            nativeQuery = true
    )
    List<Job> lockStuckJobs();
}
