package com.mallang.mallang_backend.domain.videohistory.service.impl;

import static com.mallang.mallang_backend.global.constants.AppConstants.*;
import static com.mallang.mallang_backend.global.exception.ErrorCode.*;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new ServiceException(MEMBER_NOT_FOUND));
		Videos videos = videoRepository.findById(videoId)
			.orElseThrow(() -> new ServiceException(VIDEO_ID_SEARCH_FAILED));

		videoHistoryRepository.findByMemberAndVideos(member, videos)
			.ifPresentOrElse(
				VideoHistory::updateTimestamp,
				() -> videoHistoryRepository.save(
					VideoHistory.builder()
						.member(member)
						.videos(videos)
						.build()
				)
			);

		// 초과 기록이 있으면 삭제
		removeExcessHistories(member);
	}

	/**
	 * 회원별 기록이 MAX_HISTORY_PER_MEMBER 를 초과하면
	 * 오래된 것부터 삭제
	 */
	private void removeExcessHistories(Member member) {
		long total = videoHistoryRepository.countByMember(member);
		if (total <= MAX_HISTORY_PER_MEMBER) {
			return;
		}

		int excess = (int)(total - MAX_HISTORY_PER_MEMBER);
		Pageable page = PageRequest.of(0, excess, Sort.by("lastViewedAt").ascending());
		List<VideoHistory> toDelete =
			videoHistoryRepository.findAllByMemberOrderByLastViewedAtAsc(member, page);

		videoHistoryRepository.deleteAllInBatch(toDelete);
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


	/**
	 * 페이징 조회
	 */
	@Override
	@Transactional(readOnly = true)
	public List<VideoHistoryResponse> getHistoriesByPage(Long memberId, int page, int size) {
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new ServiceException(MEMBER_NOT_FOUND));

		Pageable pageable = PageRequest.of(page, size, Sort.by("lastViewedAt").descending());
		return videoHistoryRepository
			.findAllByMemberOrderByLastViewedAtDesc(member, pageable)
			.stream()
			.map(VideoHistoryResponse::from)
			.toList();
	}
}
