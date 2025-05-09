package com.mallang.mallang_backend.domain.videohistory.service.impl;

import static com.mallang.mallang_backend.global.exception.ErrorCode.*;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.member.repository.MemberRepository;
import com.mallang.mallang_backend.domain.video.video.entity.Videos;
import com.mallang.mallang_backend.domain.video.video.repository.VideoRepository;
import com.mallang.mallang_backend.domain.video.video.service.VideoService;
import com.mallang.mallang_backend.domain.videohistory.dto.VideoHistoryResponse;
import com.mallang.mallang_backend.domain.videohistory.entity.VideoHistory;
import com.mallang.mallang_backend.domain.videohistory.repository.VideoHistoryRepository;
import com.mallang.mallang_backend.domain.videohistory.service.VideoHistoryService;
import com.mallang.mallang_backend.global.exception.ServiceException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VideoHistoryServiceImpl implements VideoHistoryService {

	private final VideoHistoryRepository videoHistoryRepository;
	private final MemberRepository memberRepository;
	private final VideoRepository videoRepository;
	private final VideoService videoService;

	/** 기록 저장(쓰기 트랜잭션) */
	@Override
	@Transactional
	public void save(Long memberId, String videoId) {
		// Member / Video 로드
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new ServiceException(MEMBER_NOT_FOUND));
		Videos videos = videoRepository.findById(videoId)
			.orElseThrow(() -> new ServiceException(VIDEO_ID_SEARCH_FAILED));

		// 조회한 비디오가 존재하는지 확인
		videoHistoryRepository.findByMemberAndVideos(member, videos)
			.ifPresentOrElse(VideoHistory::updateTimestamp, () -> {
	            // 조회한 비디오가 존재하지 않는 경우
				VideoHistory newHistory = VideoHistory.builder()
					.member(member)
					.videos(videos)
					.build();
				videoHistoryRepository.save(newHistory);
			});
	}

	/** 최근 5개 조회 */
	@Override
	@Transactional(readOnly = true)
	public List<VideoHistoryResponse> getRecentHistories(Long memberId) {
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new ServiceException(MEMBER_NOT_FOUND));

		return videoHistoryRepository
			.findTop5ByMemberOrderByLastViewedAtDesc(member)
			.stream()
            .map(VideoHistoryResponse::from)
			.toList();
	}

	/** 전체 조회 */
	@Override
	@Transactional(readOnly = true)
	public List<VideoHistoryResponse> getAllHistories(Long memberId) {
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new ServiceException(MEMBER_NOT_FOUND));

		return videoHistoryRepository
			.findAllByMemberOrderByLastViewedAtDesc(member)
			.stream()
            .map(VideoHistoryResponse::from)
			.toList();
	}
}
