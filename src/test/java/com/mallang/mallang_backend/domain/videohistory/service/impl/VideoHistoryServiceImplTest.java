package com.mallang.mallang_backend.domain.videohistory.service.impl;

import com.mallang.mallang_backend.domain.video.video.dto.VideoDetailResponse;
import com.mallang.mallang_backend.domain.video.video.entity.Videos;
import com.mallang.mallang_backend.domain.video.video.repository.VideoRepository;
import com.mallang.mallang_backend.domain.video.video.service.VideoService;
import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.member.repository.MemberRepository;
import com.mallang.mallang_backend.domain.videohistory.dto.VideoHistoryResponse;
import com.mallang.mallang_backend.domain.videohistory.entity.VideoHistory;
import com.mallang.mallang_backend.domain.videohistory.mapper.VideoHistoryMapper;
import com.mallang.mallang_backend.domain.videohistory.repository.VideoHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class VideoHistoryServiceImplTest {

	@Mock
	private VideoHistoryRepository repository;

	@Mock
	private VideoHistoryMapper mapper;

	@Mock
	private VideoService videoService;

	@Mock
	private MemberRepository memberRepository;

	@Mock
	private VideoRepository videoRepository;

	@InjectMocks
	private VideoHistoryServiceImpl service;

	@Captor
	private ArgumentCaptor<VideoHistory> historyCaptor;

	private final Long MEMBER_ID = 123L;
	private final String VIDEO_ID1 = "10L";
	private final String VIDEO_ID2 = "20L";
	private final String TITLE1 = "First Video";
	private final String THUMB1 = "http://img/first.jpg";
	private final String TITLE2 = "Second Video";
	private final String THUMB2 = "http://img/second.jpg";

	private Member member;
	private Videos videos1;
	private Videos videos2;
	private VideoHistoryResponse dto1;
	private VideoHistoryResponse dto2;

	@BeforeEach
	void setUp() {
		member = mock(Member.class);
		videos1 = mock(Videos.class);
		videos2 = mock(Videos.class);

		dto1 = new VideoHistoryResponse(VIDEO_ID1, TITLE1, THUMB1, LocalDateTime.of(2025, 4, 29, 10, 0));
		dto2 = new VideoHistoryResponse(VIDEO_ID2, TITLE2, THUMB2, LocalDateTime.of(2025, 4, 28, 9, 30));
	}

	@Test
	@DisplayName("save() 호출 시 VideoHistory 엔티티가 정상 저장된다")
	void save_shouldPersistHistory() {
		// given: video meta stub
		VideoDetailResponse detail = new VideoDetailResponse(VIDEO_ID1, TITLE1, "desc", THUMB1, "chan", null);
		given(videoService.getVideoDetail(VIDEO_ID1)).willReturn(detail);
		given(memberRepository.findById(MEMBER_ID)).willReturn(Optional.of(member));
		given(videoRepository.findById(VIDEO_ID1)).willReturn(Optional.of(videos1));

		// when
		service.save(MEMBER_ID, VIDEO_ID1);

		// then: captured entity
		then(repository).should().save(historyCaptor.capture());
		VideoHistory saved = historyCaptor.getValue();
		assertThat(saved.getMember()).isEqualTo(member);
		assertThat(saved.getVideos()).isEqualTo(videos1);
		assertThat(saved.getCreatedAt()).isNotNull();
	}

	@Test
	@DisplayName("getRecentHistories() 호출 시 최신 5개 기록을 DTO로 변환하여 반환한다")
	void getRecentHistories_shouldReturnMappedDtos() {
		// given
		VideoHistory e1 = VideoHistory.builder().member(member).videos(videos1).build();
		VideoHistory e2 = VideoHistory.builder().member(member).videos(videos2).build();
		given(memberRepository.findById(MEMBER_ID)).willReturn(Optional.of(member));
		given(repository.findTop5ByMemberOrderByCreatedAtDesc(member)).willReturn(List.of(e1, e2));
		given(mapper.toDto(e1)).willReturn(dto1);
		given(mapper.toDto(e2)).willReturn(dto2);

		// when
		List<VideoHistoryResponse> result = service.getRecentHistories(MEMBER_ID);

		// then
		assertThat(result).containsExactly(dto1, dto2);
		then(repository).should().findTop5ByMemberOrderByCreatedAtDesc(member);
		then(mapper).should(times(1)).toDto(e1);
		then(mapper).should(times(1)).toDto(e2);
	}

	@Test
	@DisplayName("getAllHistories() 호출 시 전체 기록을 DTO로 변환하여 반환한다")
	void getAllHistories_shouldReturnMappedDtos() {
		// given
		VideoHistory e1 = VideoHistory.builder().member(member).videos(videos1).build();
		VideoHistory e2 = VideoHistory.builder().member(member).videos(videos2).build();
		given(memberRepository.findById(MEMBER_ID)).willReturn(Optional.of(member));
		given(repository.findAllByMemberOrderByCreatedAtDesc(member)).willReturn(List.of(e2, e1));
		given(mapper.toDto(e2)).willReturn(dto2);
		given(mapper.toDto(e1)).willReturn(dto1);

		// when
		List<VideoHistoryResponse> result = service.getAllHistories(MEMBER_ID);

		// then
		assertThat(result).containsExactly(dto2, dto1);
		then(repository).should().findAllByMemberOrderByCreatedAtDesc(member);
		then(mapper).should(times(1)).toDto(e2);
		then(mapper).should(times(1)).toDto(e1);
	}
}
