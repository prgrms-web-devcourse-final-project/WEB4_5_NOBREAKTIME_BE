package com.mallang.mallang_backend.domain.dashboard.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LevelStatus {
	private String video;
	private String voca;
	private String grammar;
	private String quiz;
	private LocalDateTime lastUpdated;
	private Boolean remeasurable;
}
