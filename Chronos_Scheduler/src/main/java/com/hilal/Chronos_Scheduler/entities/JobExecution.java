package com.hilal.Chronos_Scheduler.entities;

import com.hilal.Chronos_Scheduler.entities.enums.ExecutionStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class JobExecution {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "jobId")
    private Job job;

    private ExecutionStatus status;
    private OffsetDateTime startedAt;
    private OffsetDateTime finishedAt;
    private OffsetDateTime lastHearbeatAt;

    private int retryNumber;

    @Column(columnDefinition = "TEXT")
    private String log;
}
