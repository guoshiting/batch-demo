package com.guo.entity;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledTaskService {
	@Autowired
    JobLauncher jobLauncher;

    @Autowired
    @Qualifier("importJob")
    Job job;
    @Autowired
    @Qualifier("importJob2")
    Job job2;

    public JobParameters jobParameters;

    @Scheduled(fixedRate = 5000)
    public void execute() throws Exception{
        jobParameters = new JobParametersBuilder().addLong("time", System.currentTimeMillis()).toJobParameters();
        jobLauncher.run(job, jobParameters);
        jobLauncher.run(job2, jobParameters);
    }
}
