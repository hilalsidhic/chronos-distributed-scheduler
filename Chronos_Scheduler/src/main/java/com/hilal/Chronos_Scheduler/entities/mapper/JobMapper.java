package com.hilal.Chronos_Scheduler.entities.mapper;

import com.hilal.Chronos_Scheduler.entities.Job;
import com.hilal.Chronos_Scheduler.entities.dtos.JobResponseDto;

public class JobMapper {
    public static void mapJobRequestDtoToJob() {

    }
    public static JobResponseDto mapJobToJobResponseDto(Job job) {
        return JobResponseDto.builder()
                .id(job.getId())
                .name(job.getName())
                .status(job.getStatus())
                .payload(job.getPayload())
                .isRecurring(job.isRecurring())
                .intervalSeconds(job.getIntervalSeconds())
                .nextExecutionTime(job.getNextExecutionTime())
                .maxRetry(job.getMaxRetry())
                .maxExecutionTime(job.getMaxExecutionTime())
                .isEnabled(job.isEnabled())
                .createdBy(job.getCreatedBy())
                .createdAt(job.getCreatedAt())
                .updatedAt(job.getUpdatedAt())
                .build();
    }
}
