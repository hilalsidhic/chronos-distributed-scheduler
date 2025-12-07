package com.hilal.Chronos_Worker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class ChronosWorkerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChronosWorkerApplication.class, args);
	}

}
