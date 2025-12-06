package com.hilal.Chronos_Worker.entities.dtos;

import com.hilal.Chronos_Worker.entities.JobExecution;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.concurrent.Future;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RunningJobContext {
    private long executionId;
    private JobExecution jobExecution;
    private Future<?> future;
    private OffsetDateTime startedAt;
    private int maxExecutionSeconds;
}