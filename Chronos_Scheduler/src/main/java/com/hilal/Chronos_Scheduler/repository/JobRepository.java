package com.hilal.Chronos_Scheduler.repository;

import com.hilal.Chronos_Scheduler.entities.Job;
import com.hilal.Chronos_Scheduler.service.JobService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {
    @Query(
            value = "SELECT * FROM job " +
                    "WHERE next_execution_time<=NOW() " +
                    "AND status = 'PENDING' " +
                    "AND is_enabled = True " +
                    "ORDER BY next_execution_time ASC "+
                    "FOR UPDATE SKIP LOCKED " +
                    "LIMIT :limit",
            nativeQuery = true
    )
    List<Job> lockPendingJobs(int limit);


    @Query(
            value = "SELECT * FROM job " +
                    "WHERE status= 'RESERVED' " +
                    "AND reserved_at <= NOW() - INTERVAL '60 seconds' "+
                    "FOR UPDATE SKIP LOCKED ",
            nativeQuery = true
    )
    List<Job> lockStuckJobs();

    @Modifying
    @Query(
            value = "UPDATE job SET is_enabled = :enabled WHERE id = :id",
            nativeQuery = true
    )
    void updateIsEnabledById(@Param("enabled") boolean enabled, @Param("id") Long id);

    @Query(
            value = "SELECT * FROM job WHERE is_enabled = True",
            nativeQuery = true
    )
    List<Job> findAllEnabled();

    @Query(
            value = "SELECT * FROM job WHERE is_deleted = False",
            nativeQuery = true
    )
    List<Job> findAllNotDeleted();

    @Modifying
    @Query(
            value = "UPDATE job SET is_deleted = :deleted WHERE id = :id",
            nativeQuery = true
    )
    void updateIsDeletedById(@Param("deleted") boolean enabled, @Param("id") Long id);
}
