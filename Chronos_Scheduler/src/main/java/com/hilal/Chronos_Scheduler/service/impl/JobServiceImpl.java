package com.hilal.Chronos_Scheduler.service.impl;

import com.hilal.Chronos_Scheduler.entities.Job;
import com.hilal.Chronos_Scheduler.entities.dtos.JobRequestDto;
import com.hilal.Chronos_Scheduler.entities.dtos.JobResponseDto;
import com.hilal.Chronos_Scheduler.entities.enums.Status;
import com.hilal.Chronos_Scheduler.factories.JobFactory;
import com.hilal.Chronos_Scheduler.entities.mapper.JobMapper;
import com.hilal.Chronos_Scheduler.repository.JobRepository;
import com.hilal.Chronos_Scheduler.service.JobExecutionService;
import com.hilal.Chronos_Scheduler.service.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class JobServiceImpl implements JobService {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private JobFactory jobFactory;

    @Override
    public JobResponseDto createJob_service(JobRequestDto jobRequestDto) {
        Job job = jobRepository.save(jobFactory.createJob(jobRequestDto, false));
        return JobMapper.mapJobToJobResponseDto(job);
    }

    @Override
    public JobResponseDto createRecurringJob_service(JobRequestDto jobRequestDto) {
        Job job = jobRepository.save(jobFactory.createJob(jobRequestDto, true));
        return JobMapper.mapJobToJobResponseDto(job);
    }

    @Override
    public JobResponseDto getJobById_service(long id) {
        Job job = jobRepository.findById(id).orElseThrow(() -> new RuntimeException("Job not found"));
        return JobMapper.mapJobToJobResponseDto(job);
    }

    @Override
    public void deleteJobById_service(long id) {
        if(!jobRepository.existsById(id)) {
            throw new RuntimeException("Job not found");
        }
        jobRepository.deleteById(id);
        return;
    }


    @Override
    public List<JobResponseDto> getAllJobs_service() {
        List<Job> AllJobs = jobRepository.findAll();
        return AllJobs.stream()
                .map(JobMapper::mapJobToJobResponseDto)
                .collect(Collectors.toList());
    }

}
