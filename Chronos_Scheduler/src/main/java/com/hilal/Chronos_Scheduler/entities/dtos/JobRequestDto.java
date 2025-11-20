package com.hilal.Chronos_Scheduler.entities.dtos;

import com.hilal.Chronos_Scheduler.entities.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JobRequestDto {
    private String name;
    private Map<String, Object> payload;
    private int intervalSeconds;
    private OffsetDateTime nextExecutionTime;
    private int maxRetry;
    private int maxExecutionTime;
}
