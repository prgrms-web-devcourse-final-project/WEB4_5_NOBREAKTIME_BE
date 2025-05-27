package com.mallang.mallang_backend.global.config;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SchedulerConfig {

	@Bean
	public ScheduledExecutorService heartbeatScheduler() {
		return Executors.newScheduledThreadPool(
			Runtime.getRuntime().availableProcessors(),
			r -> {
				Thread t = new Thread(r, "sse-heartbeat");
				t.setDaemon(true);
				return t;
			}
		);
	}
}