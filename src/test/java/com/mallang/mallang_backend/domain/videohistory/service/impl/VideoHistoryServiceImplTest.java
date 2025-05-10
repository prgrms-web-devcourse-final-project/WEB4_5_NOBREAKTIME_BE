package com.mallang.mallang_backend.domain.videohistory.service.impl;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.member.repository.MemberRepository;
import com.mallang.mallang_backend.domain.video.video.entity.Videos;
import com.mallang.mallang_backend.domain.video.video.repository.VideoRepository;
import com.mallang.mallang_backend.domain.videohistory.dto.VideoHistoryResponse;
import com.mallang.mallang_backend.domain.videohistory.entity.VideoHistory;
import com.mallang.mallang_backend.domain.videohistory.repository.VideoHistoryRepository;

@ExtendWith(MockitoExtension.class)
class VideoHistoryServiceImplTest {

	@Mock
	private VideoHistoryRepository repository;

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

		// Prepare expected DTOs with fixed timestamps
		dto1 = new VideoHistoryResponse(VIDEO_ID1, TITLE1, THUMB1, LocalDateTime.of(2025, 4, 29, 10, 0));
		dto2 = new VideoHistoryResponse(VIDEO_ID2, TITLE2, THUMB2, LocalDateTime.of(2025, 4, 28, 9, 30));
	}

	@Test
	@DisplayName("save() 호출 시 VideoHistory 엔티티가 정상 저장된다")
	void save_shouldPersistHistory() {
		// given: repositories stub
		given(memberRepository.findById(MEMBER_ID)).willReturn(Optional.of(member));
		given(videoRepository.findById(VIDEO_ID1)).willReturn(Optional.of(videos1));

		// when
		service.save(MEMBER_ID, VIDEO_ID1);

		// then: captured entity has correct fields
		then(repository).should().save(historyCaptor.capture());
		VideoHistory saved = historyCaptor.getValue();
		assertThat(saved.getMember()).isEqualTo(member);
		assertThat(saved.getVideos()).isEqualTo(videos1);
		assertThat(saved.getCreatedAt()).isNotNull();
		assertThat(saved.getLastViewedAt()).isEqualTo(saved.getCreatedAt());
	}

	@Test
	@DisplayName("getRecentHistories() 호출 시 최신 5개 기록을 DTO로 변환하여 반환한다")
	void getRecentHistories_shouldReturnMappedDtos() throws Exception {
		// given: stub Member
		given(memberRepository.findById(MEMBER_ID)).willReturn(Optional.of(member));

		// create history entities with controlled timestamps and video data
		VideoHistory e1 = VideoHistory.builder().member(member).videos(videos1).build();
		VideoHistory e2 = VideoHistory.builder().member(member).videos(videos2).build();

		// use reflection to set lastViewedAt to match dto1 and dto2
		Field fv = VideoHistory.class.getDeclaredField("lastViewedAt");
		fv.setAccessible(true);
		fv.set(e1, dto1.getLastViewedAt());
		fv.set(e2, dto2.getLastViewedAt());

		// stub video getters
		given(videos1.getId()).willReturn(VIDEO_ID1);
		given(videos1.getVideoTitle()).willReturn(TITLE1);
		given(videos1.getThumbnailImageUrl()).willReturn(THUMB1);
		given(videos2.getId()).willReturn(VIDEO_ID2);
		given(videos2.getVideoTitle()).willReturn(TITLE2);
		given(videos2.getThumbnailImageUrl()).willReturn(THUMB2);

		given(repository.findTop5ByMemberOrderByLastViewedAtDesc(member)).willReturn(List.of(e1, e2));

		// when
		List<VideoHistoryResponse> result = service.getRecentHistories(MEMBER_ID);

		// then: verify repository call and DTO contents
		then(repository).should().findTop5ByMemberOrderByLastViewedAtDesc(member);
		assertThat(result).hasSize(2);
		assertThat(result.get(0)).usingRecursiveComparison().isEqualTo(dto1);
		assertThat(result.get(1)).usingRecursiveComparison().isEqualTo(dto2);
	}

	@Test
	@DisplayName("getAllHistories() 호출 시 전체 기록을 DTO로 변환하여 반환한다")
	void getAllHistories_shouldReturnMappedDtos() throws Exception {
		// given
		given(memberRepository.findById(MEMBER_ID)).willReturn(Optional.of(member));

		VideoHistory e1 = VideoHistory.builder().member(member).videos(videos1).build();
		VideoHistory e2 = VideoHistory.builder().member(member).videos(videos2).build();

		Field fv = VideoHistory.class.getDeclaredField("lastViewedAt");
		fv.setAccessible(true);
		fv.set(e1, dto1.getLastViewedAt());
		fv.set(e2, dto2.getLastViewedAt());

		given(videos1.getId()).willReturn(VIDEO_ID1);
		given(videos1.getVideoTitle()).willReturn(TITLE1);
		given(videos1.getThumbnailImageUrl()).willReturn(THUMB1);
		given(videos2.getId()).willReturn(VIDEO_ID2);
		given(videos2.getVideoTitle()).willReturn(TITLE2);
		given(videos2.getThumbnailImageUrl()).willReturn(THUMB2);

		given(repository.findAllByMemberOrderByLastViewedAtDesc(member)).willReturn(List.of(e2, e1));

		// when
		List<VideoHistoryResponse> result = service.getAllHistories(MEMBER_ID);

		// then
		then(repository).should().findAllByMemberOrderByLastViewedAtDesc(member);
		assertThat(result).hasSize(2);
		assertThat(result.get(0)).usingRecursiveComparison().isEqualTo(dto2);
		assertThat(result.get(1)).usingRecursiveComparison().isEqualTo(dto1);
	}
}