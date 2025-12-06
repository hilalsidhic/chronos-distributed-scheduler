package com.hilal.Chronos_Worker.repositories;

import com.hilal.Chronos_Worker.entities.JobExecution;
import com.hilal.Chronos_Worker.entities.enums.ExecutionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobExecutionRepository extends JpaRepository<JobExecution, Long> {

    @Query(value = "SELECT * " +
            "FROM job_execution " +
            "WHERE status = 'PENDING' " +
            "ORDER BY created_at ASC " +
            "FOR UPDATE SKIP LOCKED " +
            "LIMIT 10",
            nativeQuery = true)
    List<JobExecution> lockPendingJobExecutions();


    @Modifying(clearAutomatically = true)
    @Query(value = """
        UPDATE job_execution
        SET last_heartbeat_at = NOW()
        WHERE id = :id
        """, nativeQuery = true)
    void updateHeartbeatTimestamp(@Param("id") Long id);


    @Query(value = """
        SELECT id
        FROM job_execution
        WHERE status = 'RUNNING'
          AND is_picked_by_worker = TRUE
          AND last_heartbeat_at < NOW() - INTERVAL '15 seconds'
        LIMIT 10
        """, nativeQuery = true)
    List<Long> findStuckJobExecutionIds();


    @Modifying(clearAutomatically = true)
    @Query(value = """
        UPDATE job_execution
        SET status = 'STUCK',
            finished_at = NOW(),
            log = COALESCE(log, '') || E'\\nMarked STUCK at ' || NOW()
        WHERE id = :id
          AND status = 'RUNNING'
          AND last_heartbeat_at < NOW() - INTERVAL '30 seconds'
        """, nativeQuery = true)
    int markStuckIfStillRunning(@Param("id") Long id);


    @Query(value = """
        SELECT status
        FROM job_execution
        WHERE id = :id
        """, nativeQuery = true)
    ExecutionStatus findStatusById(@Param("id") Long id);


    @Query(value = """
        SELECT id
        FROM job_execution
        WHERE status = 'RUNNING'
        AND started_at + (max_execution_time * INTERVAL '1 second') < NOW()
        LIMIT 10
        """, nativeQuery = true)
    List<Long> findTimedOutJobExecutionIds();


    @Modifying(clearAutomatically = true)
    @Query(value = """
        UPDATE job_execution
        SET status = 'TIMED_OUT',
            finished_at = NOW(),
            log = COALESCE(log, '') || E'\\nMarked TIMED_OUT at ' || NOW()
        WHERE id = :id
          AND status = 'RUNNING'
          AND (started_at + (max_execution_time * INTERVAL '1 second')) < NOW()
        """, nativeQuery = true)
    int markTimedOutIfStillRunning(@Param("id") Long id);
}
