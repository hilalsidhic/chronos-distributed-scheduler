package com.hilal.Chronos_Scheduler.repository;

import com.hilal.Chronos_Scheduler.config.AbstractPostgresTest;
import com.hilal.Chronos_Scheduler.entities.Job;
import com.hilal.Chronos_Scheduler.entities.JobExecution;
import com.hilal.Chronos_Scheduler.entities.enums.ExecutionStatus;
import com.hilal.Chronos_Scheduler.entities.enums.Status;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Testcontainers
class JobExecutionRepositoryTest extends AbstractPostgresTest {

    @Autowired
    private JobExecutionRepository repo;

    @Autowired
    private JobRepository jobRepo;

    // ----------------------------------------------------
    // FAILED EXECUTIONS
    // ----------------------------------------------------

    @Test
    void lockFailedJobExecutions_shouldReturnOnlyFailed() {
        JobExecution failed = createExecution(ExecutionStatus.FAILED);
        JobExecution success = createExecution(ExecutionStatus.SUCCESS);
        JobExecution timedOut = createExecution(ExecutionStatus.TIMED_OUT);

        repo.saveAll(List.of(failed, success, timedOut));

        List<JobExecution> result = repo.lockFailedJobExecutions(10);

        assertEquals(1, result.size());
        assertEquals(ExecutionStatus.FAILED, result.get(0).getStatus());
    }

    @Test
    void lockFailedJobExecutions_shouldRespectLimit() {
        JobExecution e1 = createExecution(ExecutionStatus.FAILED);
        JobExecution e2 = createExecution(ExecutionStatus.FAILED);

        repo.saveAll(List.of(e1, e2));

        List<JobExecution> result = repo.lockFailedJobExecutions(1);

        assertEquals(1, result.size());
    }


    // ----------------------------------------------------
    // TIMED OUT EXECUTIONS
    // ----------------------------------------------------

    @Test
    void lockTimedOutJobExecutions_shouldReturnOnlyTimedOut() {
        JobExecution timedOut = createExecution(ExecutionStatus.TIMED_OUT);
        JobExecution failed = createExecution(ExecutionStatus.FAILED);

        repo.saveAll(List.of(timedOut, failed));

        List<JobExecution> result = repo.lockTimedOutJobExecutions(10);

        assertEquals(1, result.size());
        assertEquals(ExecutionStatus.TIMED_OUT, result.get(0).getStatus());
    }

    @Test
    void lockTimedOutJobExecutions_shouldRespectLimit() {
        JobExecution e1 = createExecution(ExecutionStatus.TIMED_OUT);
        JobExecution e2 = createExecution(ExecutionStatus.TIMED_OUT);

        repo.saveAll(List.of(e1, e2));

        List<JobExecution> result = repo.lockTimedOutJobExecutions(1);

        assertEquals(1, result.size());
    }


    // ----------------------------------------------------
    // SUCCESS EXECUTIONS
    // ----------------------------------------------------

    @Test
    void lockCompletedJobExecutions_shouldReturnOnlySuccess() {
        JobExecution success = createExecution(ExecutionStatus.SUCCESS);
        JobExecution failed = createExecution(ExecutionStatus.FAILED);

        repo.saveAll(List.of(success, failed));

        List<JobExecution> result = repo.lockCompletedJobExecutions(10);

        assertEquals(1, result.size());
        assertEquals(ExecutionStatus.SUCCESS, result.get(0).getStatus());
    }

    @Test
    void lockCompletedJobExecutions_shouldRespectLimit() {
        JobExecution e1 = createExecution(ExecutionStatus.SUCCESS);
        JobExecution e2 = createExecution(ExecutionStatus.SUCCESS);

        repo.saveAll(List.of(e1, e2));

        List<JobExecution> result = repo.lockCompletedJobExecutions(1);

        assertEquals(1, result.size());
    }


    // ----------------------------------------------------
    // FIND BY JOB ID (ORDER + PAGINATION)
    // ----------------------------------------------------

    @Test
    void findByJobId_shouldReturnOrderedByCreatedAtDesc() {

        Job job = jobRepo.save(
                Job.builder()
                        .status(Status.PENDING)
                        .isEnabled(true)
                        .nextExecutionTime(OffsetDateTime.now())
                        .retryCount(0)
                        .maxRetry(3)
                        .build()
        );

        JobExecution e1 = createExecutionForJob(job, ExecutionStatus.SUCCESS, OffsetDateTime.now().minusMinutes(3));
        JobExecution e2 = createExecutionForJob(job, ExecutionStatus.SUCCESS, OffsetDateTime.now().minusMinutes(1));
        JobExecution e3 = createExecutionForJob(job, ExecutionStatus.SUCCESS, OffsetDateTime.now().minusMinutes(2));

        repo.saveAllAndFlush(List.of(e1, e2, e3));

        List<JobExecution> result = repo.findByJobId(job.getId(), 10, 0);

        assertEquals(3, result.size());
        assertEquals(e2.getId(), result.get(0).getId());
        assertEquals(e3.getId(), result.get(1).getId());
        assertEquals(e1.getId(), result.get(2).getId());
    }

    @Test
    void findByJobId_shouldRespectLimitAndOffset() {

        Job job = jobRepo.save(
                Job.builder()
                        .status(Status.PENDING)
                        .isEnabled(true)
                        .nextExecutionTime(OffsetDateTime.now())
                        .retryCount(0)
                        .maxRetry(3)
                        .build()
        );

        JobExecution e1 = createExecutionForJob(job, ExecutionStatus.SUCCESS, OffsetDateTime.now().minusMinutes(3));
        JobExecution e2 = createExecutionForJob(job, ExecutionStatus.SUCCESS, OffsetDateTime.now().minusMinutes(2));
        JobExecution e3 = createExecutionForJob(job, ExecutionStatus.SUCCESS, OffsetDateTime.now().minusMinutes(1));

        repo.saveAllAndFlush(List.of(e1, e2, e3));

        List<JobExecution> result = repo.findByJobId(job.getId(), 1, 1);

        assertEquals(1, result.size());
        assertEquals(e2.getId(), result.get(0).getId());
    }



    // ----------------------------------------------------
    // Helper methods
    // ----------------------------------------------------

    private JobExecution createExecution(ExecutionStatus status) {

        Job job = Job.builder()
                .status(Status.PENDING)
                .isEnabled(true)
                .nextExecutionTime(OffsetDateTime.now())
                .retryCount(0)
                .maxRetry(3)
                .build();

        jobRepo.save(job);

        return JobExecution.builder()
                .job(job)
                .status(status)
                .startedAt(OffsetDateTime.now())
                .retryNumber(0)
                .isPickedByWorker(false)
                .maxExecutionTime(30)
                .build();
    }

    private JobExecution createExecutionForJob(Job job, ExecutionStatus status, OffsetDateTime startedAt) {
        return JobExecution.builder()
                .job(job)
                .status(status)
                .startedAt(startedAt)
                .retryNumber(0)
                .isPickedByWorker(false)
                .maxExecutionTime(30)
                .build();
    }

}
