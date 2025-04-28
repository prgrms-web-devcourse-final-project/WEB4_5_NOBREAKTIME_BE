package com.mallang.mallang_backend.global.util;

import java.io.File;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.mallang.mallang_backend.global.common.Language;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TestRunner implements CommandLineRunner {

	private final ClovaSpeechClient clovaSpeechClient;

	@Override
	public void run(String... args) throws Exception {
		NestRequestEntity requestEntity = new NestRequestEntity(Language.ENGLISH);
		final String result =
			clovaSpeechClient.upload(new File("./uploads/audio_8cf7c349-a51d-4f75-9428-4259bfbdb8441745799537457.mp3"), requestEntity);
		//final String result = clovaSpeechClient.url("file URL", requestEntity);
		//final String result = clovaSpeechClient.objectStorage("Object Storage key", requestEntity);
		System.out.println(result);
	}
}
