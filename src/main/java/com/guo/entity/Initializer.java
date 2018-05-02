package com.guo.entity;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class Initializer implements CommandLineRunner {

	@Autowired
	private ScheduledTaskService service;
	
	@Override
	public void run(String... arg0) throws Exception {
		service.execute();
	}

//    @Bean
//    public Job importJob2(JobBuilderFactory jobs, Step step){
//    	return jobs.get("importJob2")
//    			.incrementer(new RunIdIncrementer())
//    			.flow(step) // 为Job指定Step
//    			.end()
//    			.listener(csvJobListener()) // 绑定监听器
//    			.build();
//    }

}
