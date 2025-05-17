package com.mallang.mallang_backend.domain.plan.entity.domain.video.subtitle.entity;

import com.mallang.mallang_backend.domain.plan.entity.domain.keyword.entity.Keyword;
import com.mallang.mallang_backend.domain.plan.entity.domain.video.video.entity.Videos;
import com.mallang.mallang_backend.global.entity.BaseTime;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@NoArgsConstructor
public class Subtitle extends BaseTime {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "subtitle_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "video_id", nullable = false)
	private Videos videos;

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

	@OneToMany(mappedBy = "subtitles", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
	private List<Keyword> keywords  = new ArrayList<>();;

	@Builder
	public Subtitle(
		Videos videos,
		String startTime,
		String endTime,
		String originalSentence,
		String translatedSentence,
		String speaker
	) {
		this.videos = videos;
		this.startTime = startTime;
		this.endTime = endTime;
		this.originalSentence = originalSentence;
		this.translatedSentence = translatedSentence;
		this.speaker = speaker;
	}
}
