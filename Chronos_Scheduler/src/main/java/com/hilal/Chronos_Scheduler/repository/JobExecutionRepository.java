package com.hilal.Chronos_Scheduler.repository;

import com.hilal.Chronos_Scheduler.entities.JobExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobExecutionRepository extends JpaRepository<JobExecution, Long> {

    // FAILED EXECUTIONS
    @Query(
            value = """
            SELECT * 
            FROM job_execution 
            WHERE status = 'FAILED'
            AND is_preserved = FALSE
            FOR UPDATE SKIP LOCKED 
            LIMIT :limit
            """,
            nativeQuery = true
    )
    List<JobExecution> lockFailedJobExecutions(@Param("limit") int limit);


    // TIMED OUT EXECUTIONS
    @Query(
            value = """
            SELECT * 
            FROM job_execution 
            WHERE (status = 'TIMED_OUT' OR status = 'STUCK')
            AND is_preserved = FALSE
            FOR UPDATE SKIP LOCKED 
            LIMIT :limit
            """,
            nativeQuery = true
    )
    List<JobExecution> lockTimedOutJobExecutions(@Param("limit") int limit);


    // SUCCESSFUL EXECUTIONS
    @Query(
            value = """
            SELECT * 
            FROM job_execution 
            WHERE status = 'SUCCESS'
            AND is_preserved = FALSE
            FOR UPDATE SKIP LOCKED 
            LIMIT :limit
            """,
            nativeQuery = true
    )
    List<JobExecution> lockCompletedJobExecutions(@Param("limit") int limit);



    // FIND JOB EXECUTION HISTORY (DESC ORDERING)
    @Query(
            value = """
            SELECT * 
            FROM job_execution 
            WHERE job_id = :jobId
            ORDER BY last_heartbeat_at DESC
            LIMIT :limit
            OFFSET :offset
            """,
            nativeQuery = true
    )
    List<JobExecution> findByJobId(
            @Param("jobId") long jobId,
            @Param("limit") long limit,
            @Param("offset") long offset
    );

    @Query(
            value = """
            SELECT je.*
            FROM job_execution je
            JOIN job j ON j.id = je.job_id
            WHERE j.is_deleted = FALSE
            ORDER BY je.last_heartbeat_at DESC
            LIMIT :limit
            OFFSET :offset
            """,
            nativeQuery = true
    )
    List<JobExecution> findAllJobExecutions(
            @Param("limit") long limit,
            @Param("offset") long offset
    );

    @Query(
            value = """
            SELECT COUNT(*)
            FROM job_execution je
            JOIN job j ON j.id = je.job_id
            WHERE j.is_deleted = FALSE
            """,
            nativeQuery = true
    )
    long countAllJobExecutions();

    @Query(
        value = """
        SELECT COUNT(*) 
        FROM job_execution je
        JOIN job j ON j.id = je.job_id
        WHERE j.is_deleted = FALSE 
        AND je.status = :status
        """,
            nativeQuery = true)
    long countByStatus(@Param("status") String status);

}
