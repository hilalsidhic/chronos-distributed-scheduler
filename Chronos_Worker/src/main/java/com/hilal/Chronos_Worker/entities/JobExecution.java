package com.hilal.Chronos_Worker.entities;

import com.hilal.Chronos_Worker.entities.enums.ExecutionStatus;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.time.OffsetDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "job_execution")
public class JobExecution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "job_id")
    private long jobId;  // <-- Using raw ID instead of Job entity

    @Enumerated(EnumType.STRING)
    private ExecutionStatus status;

    private OffsetDateTime startedAt;
    private OffsetDateTime finishedAt;

    @Column(name = "last_heartbeat_at")
    private OffsetDateTime lastHeartbeatAt;

    private int maxExecutionTime;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> payload;

    private int retryNumber;

    @Column(name = "is_picked_by_worker")
    private boolean pickedByWorker;

    private OffsetDateTime createdAt;

    @Column(columnDefinition = "TEXT")
    private String log;
}
