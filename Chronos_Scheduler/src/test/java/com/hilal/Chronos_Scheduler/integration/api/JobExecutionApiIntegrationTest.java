package com.hilal.Chronos_Scheduler.integration.api;

import com.hilal.Chronos_Scheduler.entities.Job;
import com.hilal.Chronos_Scheduler.entities.JobExecution;
import com.hilal.Chronos_Scheduler.entities.enums.ExecutionStatus;
import com.hilal.Chronos_Scheduler.integration.BaseIntegrationTest;
import com.hilal.Chronos_Scheduler.repository.JobExecutionRepository;
import com.hilal.Chronos_Scheduler.repository.JobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;

import java.time.OffsetDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
class JobExecutionApiIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private JobExecutionRepository jobExecutionRepository;

    @BeforeEach
    void setup() {
        jobExecutionRepository.deleteAll();
        jobRepository.deleteAll();
    }

    private JobExecution saveExecution(long jobId) {
        Job job = jobRepository.save(
                Job.builder()
                        .name("test")
                        .status(com.hilal.Chronos_Scheduler.entities.enums.Status.PENDING)
                        .isEnabled(true)
                        .nextExecutionTime(OffsetDateTime.now())
                        .retryCount(0)
                        .maxRetry(3)
                        .createdAt(OffsetDateTime.now())
                        .updatedAt(OffsetDateTime.now())
                        .build()
        );

        JobExecution exec = JobExecution.builder()
                .job(job)
                .status(ExecutionStatus.SUCCESS)
                .startedAt(OffsetDateTime.now().minusMinutes(1))
                .finishedAt(OffsetDateTime.now())
                .isPickedByWorker(true)
                .retryNumber(0)
                .build();

        return jobExecutionRepository.save(exec);
    }

    @Test
    void getExecutionsByJobId_shouldReturnList() throws Exception {
        JobExecution exec = saveExecution(1L);

        mockMvc.perform(get("/jobs/" + exec.getJob().getId() + "/executions")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(exec.getId()))
                .andExpect(jsonPath("$[0].status").value("SUCCESS"));
    }

    @Test
    void getExecutionsByJobId_shouldReturnEmptyListIfNone() throws Exception {

        mockMvc.perform(get("/jobs/999/executions")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getExecutionById_shouldReturnExecution() throws Exception {
        JobExecution exec = saveExecution(1L);

        mockMvc.perform(get("/executions/" + exec.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(exec.getId()))
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    void getExecutionById_shouldReturn404_WhenNotFound() throws Exception {

        mockMvc.perform(get("/executions/99999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
