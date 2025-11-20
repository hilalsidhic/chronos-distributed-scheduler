package com.hilal.Chronos_Scheduler.factories;

import com.hilal.Chronos_Scheduler.entities.Job;
import com.hilal.Chronos_Scheduler.entities.dtos.JobRequestDto;

public interface JobFactory {
    Job createJob(JobRequestDto jobRequestDto, boolean recurring);
}
