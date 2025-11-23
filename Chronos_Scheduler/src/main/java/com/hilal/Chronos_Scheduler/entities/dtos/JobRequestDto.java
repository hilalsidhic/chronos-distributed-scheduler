package com.hilal.Chronos_Scheduler.entities.dtos;

import com.hilal.Chronos_Scheduler.entities.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;

import java.time.OffsetDateTime;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JobRequestDto {
    @NotBlank(message = "Job name cannot be empty")
    private String name;

    @NotNull(message = "payload cannot be null")
    private Map<String, Object> payload;

    @Min(value = 0, message = "Interval seconds must be >= 0")
    private int intervalSeconds;

    @Min(value = 0, message = "Max retry must be >= 0")
    private int maxRetry;

    @Min(value = 1, message = "Max execution time must be >= 1")
    private int maxExecutionTime;
    private OffsetDateTime nextExecutionTime;
}
