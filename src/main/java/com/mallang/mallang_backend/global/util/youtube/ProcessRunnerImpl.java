package com.mallang.mallang_backend.global.util.youtube;

import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ProcessRunnerImpl implements ProcessRunner {
	@Override
	public Process runProcess(String... command) throws IOException {
		return new ProcessBuilder(command).redirectErrorStream(true).start();
	}
}
