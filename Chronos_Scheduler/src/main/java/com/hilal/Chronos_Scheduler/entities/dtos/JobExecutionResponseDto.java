package com.hilal.Chronos_Scheduler.entities.dtos;

import com.hilal.Chronos_Scheduler.entities.enums.ExecutionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JobExecutionResponseDto {
    private Long id;
    private Long jobId;
    private String jobName;
    private ExecutionStatus status;
    private OffsetDateTime startedAt;
    private OffsetDateTime finishedAt;
    private String log;
}
