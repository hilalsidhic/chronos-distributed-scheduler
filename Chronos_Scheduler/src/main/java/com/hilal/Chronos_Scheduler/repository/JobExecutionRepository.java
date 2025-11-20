package com.hilal.Chronos_Scheduler.repository;

import com.hilal.Chronos_Scheduler.entities.JobExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobExecutionRepository extends JpaRepository<JobExecution, Long> {
}
