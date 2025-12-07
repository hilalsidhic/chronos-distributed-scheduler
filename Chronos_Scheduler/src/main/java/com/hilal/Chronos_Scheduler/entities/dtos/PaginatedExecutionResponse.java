package com.hilal.Chronos_Scheduler.entities.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaginatedExecutionResponse {
    private List<JobExecutionResponseDto> items;
    private long total;
    private long limit;
    private long offset;
    private boolean hasNext;
}
