package com.mallang.mallang_backend.domain.plan.entity.domain.stt.converter;

import java.io.IOException;

public interface TranscriptParser {
	Transcript parseTranscriptJson(String json) throws IOException;
}
