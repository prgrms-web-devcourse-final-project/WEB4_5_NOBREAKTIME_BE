package com.mallang.mallang_backend.global.util.clova;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Diarization {
	private Boolean enable = Boolean.FALSE;
	private Integer speakerCountMin;
	private Integer speakerCountMax;
}
