package com.hilal.Chronos_Scheduler.entities.dtos;

import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExecutionStatsResponse {

    private long totalExecutions;
    private long totalSuccess;
    private long totalFailed;
    private long totalRunning;
    private long totalStuck;
    private long totalTimedOut;

    private List<JobExecutionResponseDto> recentExecutions;
}
