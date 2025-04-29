package com.mallang.mallang_backend.global.util;

import java.io.IOException;

import org.springframework.stereotype.Component;

@Component
public class ProcessRunnerImpl implements ProcessRunner {
	@Override
	public Process runProcess(String... command) throws IOException {
		return new ProcessBuilder(command).redirectErrorStream(true).start();
	}
}
