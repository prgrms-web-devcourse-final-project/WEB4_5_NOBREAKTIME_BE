package com.mallang.mallang_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableRetry
@EnableScheduling
@SpringBootApplication
public class MallangBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(MallangBackendApplication.class, args);
	}

}
