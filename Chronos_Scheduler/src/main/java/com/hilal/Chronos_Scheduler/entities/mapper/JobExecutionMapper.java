package com.hilal.Chronos_Scheduler.entities.mapper;

import com.hilal.Chronos_Scheduler.entities.JobExecution;
import com.hilal.Chronos_Scheduler.entities.dtos.JobExecutionResponseDto;

public class JobExecutionMapper {
    public static JobExecutionResponseDto toJobExecutionResponseDto(JobExecution jobExecution) {
        return JobExecutionResponseDto.builder()
                .id(jobExecution.getId())
                .jobId(jobExecution.getJob().getId())
                .jobName(jobExecution.getJob().getName())
                .status(jobExecution.getStatus())
                .startedAt(jobExecution.getStartedAt())
                .finishedAt(jobExecution.getFinishedAt())
                .log(jobExecution.getLog())
                .build();
    }
}
