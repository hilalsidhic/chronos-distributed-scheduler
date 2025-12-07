package com.hilal.Chronos_Scheduler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class ChronosSchedulerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChronosSchedulerApplication.class, args);
	}

}
