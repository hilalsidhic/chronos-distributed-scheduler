package com.hilal.Chronos_Scheduler.repository;

import com.hilal.Chronos_Scheduler.config.AbstractPostgresTest;
import com.hilal.Chronos_Scheduler.config.ConcurrencyJobRunner;
import com.hilal.Chronos_Scheduler.entities.Job;
import com.hilal.Chronos_Scheduler.entities.enums.Status;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Testcontainers
@Import(ConcurrencyJobRunner.class)
class JobRepositoryTest extends AbstractPostgresTest {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private ConcurrencyJobRunner concurrencyJobRunner;

    @Test
    void lockPendingJobs_shouldReturnOnlyPendingJobsOrdered() {

        // Job 1
        Job a = Job.builder()
                .status(Status.PENDING)
                .nextExecutionTime(OffsetDateTime.now().minusMinutes(5))
                .retryCount(0)
                .maxRetry(3)
                .isEnabled(true)
                .build();

        // Job 2
        Job b = Job.builder()
                .status(Status.PENDING)
                .nextExecutionTime(OffsetDateTime.now().minusMinutes(1))
                .retryCount(0)
                .maxRetry(3)
                .isEnabled(true)
                .build();

        // Non-pending job
        Job c = Job.builder()
                .status(Status.RUNNING)
                .nextExecutionTime(OffsetDateTime.now().minusMinutes(10))
                .retryCount(0)
                .maxRetry(3)
                .isEnabled(true)
                .build();

        jobRepository.saveAll(List.of(a, b, c));

        List<Job> result = jobRepository.lockPendingJobs(10);

        assertEquals(2, result.size());
        assertEquals(a.getId(), result.get(0).getId());
        assertEquals(b.getId(), result.get(1).getId());
    }
    @Test
    void lockPendingJobs_shouldRespectLimit() {
        Job a = Job.builder()
                .status(Status.PENDING)
                .nextExecutionTime(OffsetDateTime.now().minusMinutes(5))
                .retryCount(0).maxRetry(3).isEnabled(true)
                .build();

        Job b = Job.builder()
                .status(Status.PENDING)
                .nextExecutionTime(OffsetDateTime.now().minusMinutes(3))
                .retryCount(0).maxRetry(3).isEnabled(true)
                .build();

        Job c = Job.builder()
                .status(Status.PENDING)
                .nextExecutionTime(OffsetDateTime.now().minusMinutes(1))
                .retryCount(0).maxRetry(3).isEnabled(true)
                .build();

        jobRepository.saveAll(List.of(a, b, c));

        List<Job> result = jobRepository.lockPendingJobs(1);

        assertEquals(1, result.size());
        assertEquals(a.getId(), result.get(0).getId()); // earliest
    }

    @Test
    void lockPendingJobs_shouldExcludeJobsWithMaxRetryReached() {
        Job a = Job.builder()
                .status(Status.PENDING)
                .nextExecutionTime(OffsetDateTime.now().minusMinutes(5))
                .retryCount(3).maxRetry(3)
                .isEnabled(true)
                .build(); // should be excluded

        Job b = Job.builder()
                .status(Status.PENDING)
                .nextExecutionTime(OffsetDateTime.now().minusMinutes(3))
                .retryCount(0).maxRetry(3)
                .isEnabled(true)
                .build(); // should be included

        jobRepository.saveAll(List.of(a, b));

        List<Job> result = jobRepository.lockPendingJobs(10);

        assertEquals(1, result.size());
        assertEquals(b.getId(), result.get(0).getId());
    }

    @Test
    void lockPendingJobs_shouldExcludeDisabledJobs() {
        Job a = Job.builder()
                .status(Status.PENDING)
                .nextExecutionTime(OffsetDateTime.now().minusMinutes(5))
                .retryCount(0).maxRetry(3)
                .isEnabled(false) // disabled
                .build();

        Job b = Job.builder()
                .status(Status.PENDING)
                .nextExecutionTime(OffsetDateTime.now().minusMinutes(4))
                .retryCount(0).maxRetry(3)
                .isEnabled(true)
                .build();

        jobRepository.saveAll(List.of(a, b));

        List<Job> result = jobRepository.lockPendingJobs(10);

        assertEquals(1, result.size());
        assertEquals(b.getId(), result.get(0).getId());
    }

    @Test
    void lockPendingJobs_shouldExcludeJobsScheduledInFuture() {
        Job a = Job.builder()
                .status(Status.PENDING)
                .nextExecutionTime(OffsetDateTime.now().plusMinutes(5)) // future
                .retryCount(0).maxRetry(3)
                .isEnabled(true)
                .build();

        jobRepository.save(a);

        List<Job> result = jobRepository.lockPendingJobs(10);

        assertTrue(result.isEmpty());
    }

    @Test
    void lockStuckJobs_shouldReturnOnlyJobsReservedOver60SecondsAgo() {
        Job oldReserved = Job.builder()
                .status(Status.RESERVED)
                .reservedAt(OffsetDateTime.now().minusMinutes(2)) // older than 60 sec
                .nextExecutionTime(OffsetDateTime.now())
                .retryCount(0).maxRetry(3).isEnabled(true)
                .build();

        Job recentReserved = Job.builder()
                .status(Status.RESERVED)
                .reservedAt(OffsetDateTime.now().minusSeconds(30)) // too recent
                .nextExecutionTime(OffsetDateTime.now())
                .retryCount(0).maxRetry(3).isEnabled(true)
                .build();

        Job pendingJob = Job.builder()
                .status(Status.PENDING)
                .reservedAt(null)
                .nextExecutionTime(OffsetDateTime.now())
                .retryCount(0).maxRetry(3).isEnabled(true)
                .build();

        jobRepository.saveAll(List.of(oldReserved, recentReserved, pendingJob));

        List<Job> result = jobRepository.lockStuckJobs();

        assertEquals(1, result.size());
        assertEquals(oldReserved.getId(), result.get(0).getId());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void lockPendingJobs_shouldNotReturnSameJobAcrossTwoConcurrentTransactions() throws Exception {

        Job j1 = Job.builder()
                .status(Status.PENDING)
                .nextExecutionTime(OffsetDateTime.now().minusMinutes(5))
                .retryCount(0).maxRetry(5).isEnabled(true)
                .build();

        Job j2 = Job.builder()
                .status(Status.PENDING)
                .nextExecutionTime(OffsetDateTime.now().minusMinutes(4))
                .retryCount(0).maxRetry(5).isEnabled(true)
                .build();

        jobRepository.saveAll(List.of(j1, j2));

        ExecutorService executor = Executors.newFixedThreadPool(2);

        Callable<List<Job>> worker1 = () -> concurrencyJobRunner.fetchLockedJobs(1);
        Callable<List<Job>> worker2 = () -> concurrencyJobRunner.fetchLockedJobs(1);

        Future<List<Job>> f1 = executor.submit(worker1);
        Future<List<Job>> f2 = executor.submit(worker2);

        List<Job> worker1Jobs = f1.get();
        List<Job> worker2Jobs = f2.get();

        executor.shutdown();

        assertEquals(1, worker1Jobs.size());
        assertEquals(1, worker2Jobs.size());
        assertNotEquals(worker1Jobs.get(0).getId(), worker2Jobs.get(0).getId());
    }
    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void lockPendingJobs_shouldSkipLockedRowsInOtherTransaction() throws Exception {

        Job job = Job.builder()
                .status(Status.PENDING)
                .nextExecutionTime(OffsetDateTime.now().minusMinutes(5))
                .retryCount(0).maxRetry(5).isEnabled(true)
                .build();

        jobRepository.save(job);

        ExecutorService executor = Executors.newFixedThreadPool(2);

        CountDownLatch latch = new CountDownLatch(1);

        Callable<List<Job>> worker1 = () -> {
            latch.countDown();               // signal Worker1 started
            return concurrencyJobRunner.fetchLockedJobs(10);
        };

        Callable<List<Job>> worker2 = () -> {
            latch.await();                   // ensure Worker1 started first
            return concurrencyJobRunner.fetchLockedJobs(10);
        };

        Future<List<Job>> f1 = executor.submit(worker1);
        Future<List<Job>> f2 = executor.submit(worker2);

        List<Job> r1 = f1.get();
        List<Job> r2 = f2.get();

        executor.shutdown();
        executor.awaitTermination(2, TimeUnit.SECONDS);

        assertEquals(1, r1.size());
        assertTrue(r2.isEmpty());
    }

}