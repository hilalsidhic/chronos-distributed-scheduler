package com.hilal.Chronos_Scheduler.integration.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hilal.Chronos_Scheduler.entities.Job;
import com.hilal.Chronos_Scheduler.entities.enums.Status;
import com.hilal.Chronos_Scheduler.integration.BaseIntegrationTest;
import com.hilal.Chronos_Scheduler.repository.JobRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
class JobApiIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void cleanDB() {
        jobRepository.deleteAll();
    }

    // -------------------------------------------------------
    // 1. CREATE JOB
    // -------------------------------------------------------
    @Test
    @DisplayName("POST /jobs → should create job in DB and return DTO")
    void createJob_shouldPersistJobAndReturnResponse() throws Exception {

        String requestJson = """
        {
          "name": "example-job",
          "payload": { "x": 10 },
          "intervalSeconds": 0,
          "maxRetry": 3,
          "maxExecutionTime": 30
        }
        """;

        mockMvc.perform(post("/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("example-job"))
                .andExpect(jsonPath("$.status").exists());

        assert(jobRepository.count() == 1);
        assert(jobRepository.findAll().get(0).getName().equals("example-job"));
    }

    // -------------------------------------------------------
    // 2. CREATE RECURRING JOB
    // -------------------------------------------------------
    @Test
    @DisplayName("POST /jobs/recurring → should create recurring job")
    void createRecurringJob_shouldPersistAndReturnResponse() throws Exception {

        String requestJson = """
        {
          "name": "recurring-job",
          "payload": { "data": 99 },
          "intervalSeconds": 60,
          "maxRetry": 5,
          "maxExecutionTime": 20
        }
        """;

        mockMvc.perform(post("/jobs/recurring")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("recurring-job"))
                .andExpect(jsonPath("$.recurring").value(true));

        Job saved = jobRepository.findAll().get(0);
        assert(saved.isRecurring());
    }

    // -------------------------------------------------------
    // 3. GET JOB BY ID
    // -------------------------------------------------------
    @Test
    @DisplayName("GET /jobs/{id} → should return job")
    void getJobById_shouldReturnJob() throws Exception {

        Job saved = jobRepository.save(
                Job.builder()
                        .name("hello")
                        .maxRetry(3)
                        .status(Status.PENDING)
                        .isEnabled(true)
                        .build()
        );

        mockMvc.perform(get("/jobs/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("hello"));
    }

    // -------------------------------------------------------
    // 4. GET JOB NOT FOUND
    // -------------------------------------------------------
    @Test
    @DisplayName("GET /jobs/{id} → should return 404 if job does not exist")
    void getJobById_shouldReturn404() throws Exception {

        mockMvc.perform(get("/jobs/99999"))
                .andExpect(status().isNotFound());
    }

    // -------------------------------------------------------
    // 5. GET ALL JOBS
    // -------------------------------------------------------
    @Test
    @DisplayName("GET /jobs → should return list of jobs")
    void getAllJobs_shouldReturnJobs() throws Exception {

        jobRepository.save(Job.builder()
                .name("job-A").maxRetry(3).status(Status.PENDING).isEnabled(true).build());

        jobRepository.save(Job.builder()
                .name("job-B").maxRetry(3).status(Status.PENDING).isEnabled(true).build());

        mockMvc.perform(get("/jobs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    // -------------------------------------------------------
    // 6. DELETE JOB
    // -------------------------------------------------------
    @Test
    @DisplayName("DELETE /jobs/{id} → should remove job")
    void deleteJob_shouldRemoveJob() throws Exception {

        Job saved = jobRepository.save(Job.builder()
                .name("sample")
                .maxRetry(2)
                .status(Status.PENDING)
                .isEnabled(true)
                .build()
        );

        mockMvc.perform(delete("/jobs/" + saved.getId()))
                .andExpect(status().isOk());

        assert(jobRepository.count() == 0);
    }

    // -------------------------------------------------------
    // 7. BAD REQUEST → missing required fields
    // -------------------------------------------------------
    @Test
    @DisplayName("POST /jobs → should return 400 for invalid input")
    void createJob_shouldFailValidation() throws Exception {

        String invalidJson = """
        {
          "name": "",
          "maxRetry": -5
        }
        """;

        mockMvc.perform(post("/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }
}
