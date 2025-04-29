package com.mallang.mallang_backend.domain.video.subtitle.entity;

import com.mallang.mallang_backend.domain.video.video.entity.Video;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
public class Subtitle {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "subtitle_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "video_id", nullable = false)
	private Video video;

	@Column(nullable = false)
	private String startTime;

	@Column(nullable = false)
	private String endTime;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String originalSentence;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String translatedSentence;

	@Column(nullable = false)
	private String speaker;

	@Builder
	public Subtitle(
		Video video,
		String startTime,
		String endTime,
		String originalSentence,
		String translatedSentence,
		String speaker
	) {
		this.video = video;
		this.startTime = startTime;
		this.endTime = endTime;
		this.originalSentence = originalSentence;
		this.translatedSentence = translatedSentence;
		this.speaker = speaker;
	}
}
