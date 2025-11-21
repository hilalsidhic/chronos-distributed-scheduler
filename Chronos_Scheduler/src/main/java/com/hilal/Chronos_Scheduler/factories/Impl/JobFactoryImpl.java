package com.hilal.Chronos_Scheduler.factories.Impl;

import com.hilal.Chronos_Scheduler.entities.Job;
import com.hilal.Chronos_Scheduler.entities.dtos.JobRequestDto;
import com.hilal.Chronos_Scheduler.entities.enums.Status;
import com.hilal.Chronos_Scheduler.factories.JobFactory;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
public class JobFactoryImpl implements JobFactory {
    @Override
    public Job createJob(JobRequestDto jobRequestDto, boolean recurring) {
        OffsetDateTime nextExecutionTime;
        if(jobRequestDto.getNextExecutionTime() != null) {
            nextExecutionTime = jobRequestDto.getNextExecutionTime();
        } else {
            nextExecutionTime = OffsetDateTime.now().plusMinutes(1);
        }
        int intervalSeconds = recurring ? jobRequestDto.getIntervalSeconds() : 0;
        return Job.builder()
                .name(jobRequestDto.getName())
                .payload(jobRequestDto.getPayload())
                .status(Status.PENDING)
                .intervalSeconds(intervalSeconds)
                .nextExecutionTime(nextExecutionTime)
                .maxRetry(jobRequestDto.getMaxRetry())
                .maxExecutionTime(jobRequestDto.getMaxExecutionTime())
                .isRecurring(recurring)
                .retryCount(0)
                .isEnabled(true)
                .createdBy("dummyUser") // In real scenarios, fetch from authenticated user context
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }
}
