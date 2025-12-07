package com.hilal.Chronos_Worker.unit.repository;

import com.hilal.Chronos_Worker.config.AbstractPostgresTest;
import com.hilal.Chronos_Worker.entities.JobExecution;
import com.hilal.Chronos_Worker.entities.enums.ExecutionStatus;
import com.hilal.Chronos_Worker.repositories.JobExecutionRepository;
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

    // ---------------------------------------------------------
    // lockPendingJobExecutions()
    // ---------------------------------------------------------

    @Test
    void lockPendingJobExecutions_shouldReturnOnlyPending() {
        JobExecution pending = createExecution(ExecutionStatus.PENDING);
        JobExecution running = createExecution(ExecutionStatus.RUNNING);

        repo.saveAllAndFlush(List.of(pending, running));

        List<JobExecution> result = repo.lockPendingJobExecutions();

        assertEquals(1, result.size());
        assertEquals(ExecutionStatus.PENDING, result.get(0).getStatus());
    }

    @Test
    void lockPendingJobExecutions_shouldReturnInCreatedAtOrderAscending() throws InterruptedException {
        JobExecution e1 = createExecution(ExecutionStatus.PENDING);
        Thread.sleep(5);
        JobExecution e2 = createExecution(ExecutionStatus.PENDING);
        repo.saveAllAndFlush(List.of(e1, e2));

        List<JobExecution> result = repo.lockPendingJobExecutions();

        assertEquals(e1.getId(), result.get(0).getId());
        assertEquals(e2.getId(), result.get(1).getId());
    }

    // ---------------------------------------------------------
    // updateHeartbeatTimestamp()
    // ---------------------------------------------------------

    @Test
    void updateHeartbeatTimestamp_shouldUpdateLastHeartbeat() {
        JobExecution je = createExecution(ExecutionStatus.RUNNING);
        repo.saveAndFlush(je);

        repo.updateHeartbeatTimestamp(je.getId());
        JobExecution updated = repo.findById(je.getId()).orElseThrow();

        assertNotNull(updated.getLastHeartbeatAt());
    }

    // ---------------------------------------------------------
    // findStuckJobExecutionIds()
    // ---------------------------------------------------------

    @Test
    void findStuckJobExecutionIds_shouldReturnOnlyStuck() {
        JobExecution stale = createExecution(ExecutionStatus.RUNNING);
        stale.setPickedByWorker(true);
        stale.setLastHeartbeatAt(OffsetDateTime.now().minusSeconds(20));

        JobExecution healthy = createExecution(ExecutionStatus.RUNNING);
        healthy.setPickedByWorker(true);
        healthy.setLastHeartbeatAt(OffsetDateTime.now());

        repo.saveAllAndFlush(List.of(stale, healthy));

        List<Long> stuck = repo.findStuckJobExecutionIds();

        assertEquals(1, stuck.size());
        assertEquals(stale.getId(), stuck.get(0));
    }

    // ---------------------------------------------------------
    // markStuckIfStillRunning()
    // ---------------------------------------------------------

    @Test
    void markStuckIfStillRunning_shouldUpdateStatusOnlyIfStale() {
        JobExecution stale = createExecution(ExecutionStatus.RUNNING);
        stale.setPickedByWorker(true);

        // Strict normalization + safe buffer
        stale.setLastHeartbeatAt(
                OffsetDateTime.now().minusSeconds(30).withNano(0)
        );

        repo.saveAndFlush(stale);

        int updated = repo.markStuckIfStillRunning(stale.getId());
        assertEquals(1, updated);

        JobExecution after = repo.findById(stale.getId()).orElseThrow();
        assertEquals(ExecutionStatus.STUCK, after.getStatus());
    }


    @Test
    void markStuckIfStillRunning_shouldNotUpdateIfNotRunning() {
        JobExecution success = createExecution(ExecutionStatus.SUCCESS);
        success.setPickedByWorker(true);
        success.setLastHeartbeatAt(OffsetDateTime.now().minusSeconds(20));
        repo.saveAndFlush(success);

        int updated = repo.markStuckIfStillRunning(success.getId());
        assertEquals(0, updated);
    }

    // ---------------------------------------------------------
    // findTimedOutJobExecutionIds()
    // ---------------------------------------------------------

    @Test
    void findTimedOutJobExecutionIds_shouldReturnOnlyTimedOut() {
        JobExecution timedOut = createExecution(ExecutionStatus.RUNNING);
        timedOut.setStartedAt(OffsetDateTime.now().minusSeconds(100));
        timedOut.setMaxExecutionTime(30);

        JobExecution ok = createExecution(ExecutionStatus.RUNNING);
        ok.setStartedAt(OffsetDateTime.now());
        ok.setMaxExecutionTime(300);

        repo.saveAllAndFlush(List.of(timedOut, ok));

        List<Long> result = repo.findTimedOutJobExecutionIds();

        assertEquals(1, result.size());
        assertEquals(timedOut.getId(), result.get(0));
    }

    // ---------------------------------------------------------
    // markTimedOutIfStillRunning()
    // ---------------------------------------------------------

    @Test
    void markTimedOutIfStillRunning_shouldUpdateOnlyExpiredRunningJobs() {
        JobExecution expired = createExecution(ExecutionStatus.RUNNING);
        expired.setStartedAt(OffsetDateTime.now().minusSeconds(100));
        expired.setMaxExecutionTime(20);
        repo.saveAndFlush(expired);

        int updated = repo.markTimedOutIfStillRunning(expired.getId());
        assertEquals(1, updated);

        JobExecution after = repo.findById(expired.getId()).orElseThrow();
        assertEquals(ExecutionStatus.TIMED_OUT, after.getStatus());
    }

    @Test
    void markTimedOutIfStillRunning_shouldNotUpdateIfStatusNotRunning() {
        JobExecution notRunning = createExecution(ExecutionStatus.SUCCESS);
        notRunning.setStartedAt(OffsetDateTime.now().minusSeconds(100));
        notRunning.setMaxExecutionTime(20);
        repo.saveAndFlush(notRunning);

        int updated = repo.markTimedOutIfStillRunning(notRunning.getId());
        assertEquals(0, updated);
    }

    // ---------------------------------------------------------
    // findStatusById()
    // ---------------------------------------------------------

    @Test
    void findStatusById_shouldReturnCorrectStatus() {
        JobExecution je = createExecution(ExecutionStatus.FAILED);
        repo.saveAndFlush(je);

        ExecutionStatus status = repo.findStatusById(je.getId());
        assertEquals(ExecutionStatus.FAILED, status);
    }

    // ---------------------------------------------------------
    // Helper
    // ---------------------------------------------------------

    private JobExecution createExecution(ExecutionStatus status) {
        JobExecution je = new JobExecution();
        je.setStatus(status);
        je.setCreatedAt(OffsetDateTime.now());
        je.setStartedAt(OffsetDateTime.now());
        je.setRetryNumber(0);
        je.setPickedByWorker(false);
        je.setMaxExecutionTime(60);
        je.setLog("");
        return je;
    }
    private JobExecution createExecution() {
        return JobExecution.builder()
                .jobId(1L)
                .status(ExecutionStatus.RUNNING)
                .startedAt(OffsetDateTime.now())
                .lastHeartbeatAt(OffsetDateTime.now().minusHours(2))
                .pickedByWorker(true)
                .maxExecutionTime(30)
                .retryNumber(0)
                .createdAt(OffsetDateTime.now())
                .log("")
                .build();
    }

}
