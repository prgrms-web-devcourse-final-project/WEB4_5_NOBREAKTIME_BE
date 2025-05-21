package com.mallang.mallang_backend.global.util.sse;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class SseEmitterManager {
	private ConcurrentHashMap<String, SseEmitter> emitters = new ConcurrentHashMap<>();

	public SseEmitter createEmitter(String userId) {
		SseEmitter emitter = new SseEmitter(0L);
		emitters.put(userId, emitter);

		emitter.onCompletion(() -> emitters.remove(userId));
		emitter.onTimeout(() -> emitters.remove(userId));
		emitter.onError((e) -> emitters.remove(userId));

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
