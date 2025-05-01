package com.mallang.mallang_backend.global.util.youtube;

import java.io.IOException;

public interface ProcessRunner {
	Process runProcess(String... command) throws IOException;
}