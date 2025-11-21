package com.hilal.Chronos_Scheduler.entities.dtos;

import com.hilal.Chronos_Scheduler.entities.enums.Status;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.time.OffsetDateTime;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JobResponseDto {
    private long id;
    private String name;
    private Status status;
    private Map<String, Object> payload;
    private boolean isRecurring;
    private int intervalSeconds;
    private OffsetDateTime nextExecutionTime;
    private int maxRetry;
    private int maxExecutionTime;

    private boolean isEnabled;

    private String createdBy;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
