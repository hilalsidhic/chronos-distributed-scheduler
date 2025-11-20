package com.hilal.Chronos_Scheduler.entities;

import com.fasterxml.jackson.annotation.JsonTypeId;
import com.hilal.Chronos_Scheduler.entities.enums.Status;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.time.OffsetDateTime;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Job {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private Status status;

    private String name;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> payload;

    private boolean isRecurring;
    private int intervalSeconds;
    private OffsetDateTime nextExecutionTime;
    private int retryCount;
    private int maxRetry;
    private int maxExecutionTime;

    private boolean isEnabled;
    private boolean isPickedByWorker;

    private String createdBy;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
