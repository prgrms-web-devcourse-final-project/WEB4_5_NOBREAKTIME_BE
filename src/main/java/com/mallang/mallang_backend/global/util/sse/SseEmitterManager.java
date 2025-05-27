package com.mallang.mallang_backend.global.util.sse;

import static com.mallang.mallang_backend.global.constants.AppConstants.*;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Qualifier;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Component
public class SseEmitterManager {
	private ConcurrentHashMap<String, SseEmitter> emitters = new ConcurrentHashMap<>();
	private final ScheduledExecutorService scheduler;

	public SseEmitterManager(@Qualifier("heartbeatScheduler") ScheduledExecutorService scheduler) {
		this.scheduler = scheduler;
	}

	public SseEmitter createEmitter(String userId) {
		SseEmitter emitter = new SseEmitter(-1L);
		emitters.put(userId, emitter);

		//heartbeat 설정
		final ScheduledFuture<?>[] futureHolder = new ScheduledFuture<?>[1];
		futureHolder[0] = scheduler.scheduleAtFixedRate(() -> {
			try {
				emitter.send(SseEmitter.event().comment("ping"));
			} catch (IOException e) {
				// 전송 실패 시 스케줄 취소
				futureHolder[0].cancel(true);
			}
		}, HEARTBEAT_INTERVAL_SEC, HEARTBEAT_INTERVAL_SEC, TimeUnit.SECONDS);

		// onCompletion/onTimeout/onError 에서도 동일하게 취소
		emitter.onCompletion(() -> {
			futureHolder[0].cancel(true);
			emitters.remove(userId);
		});
		emitter.onTimeout(() -> {
			futureHolder[0].cancel(true);
			emitters.remove(userId);
		});
		emitter.onError(e -> {
			futureHolder[0].cancel(true);
			emitters.remove(userId);
		});

		return emitter;
	}

	public <T> void sendTo(String userId, String name, T data) {
		SseEmitter emitter = emitters.get(userId);
		if (emitter != null) {
			try {
				emitter.send(SseEmitter.event().name(name).data(data));
			} catch (Exception e) {
				emitters.remove(userId);
			}
		}
	}

	public void removeEmitter(String userId) {
		if (emitters.containsKey(userId)) {
			emitters.get(userId).complete();
			emitters.remove(userId);
		}
	}
}
