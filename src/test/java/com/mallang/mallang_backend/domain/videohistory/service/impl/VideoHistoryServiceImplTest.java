package com.mallang.mallang_backend.domain.videohistory.service.impl;

import com.mallang.mallang_backend.domain.videohistory.dto.VideoHistoryResponse;
import com.mallang.mallang_backend.domain.videohistory.entity.VideoHistory;
import com.mallang.mallang_backend.domain.videohistory.mapper.VideoHistoryMapper;
import com.mallang.mallang_backend.domain.videohistory.repository.VideoHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class VideoHistoryServiceImplTest {

	@Mock
	private VideoHistoryRepository repository;

	@Mock
	private VideoHistoryMapper mapper;

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

	private VideoHistoryResponse dto1;
	private VideoHistoryResponse dto2;

	@BeforeEach
	void setUp() {
		LocalDateTime now1 = LocalDateTime.of(2025, 4, 29, 10, 0);
		LocalDateTime now2 = LocalDateTime.of(2025, 4, 28, 9, 30);

		dto1 = new VideoHistoryResponse(VIDEO_ID1, TITLE1, THUMB1, now1);
		dto2 = new VideoHistoryResponse(VIDEO_ID2, TITLE2, THUMB2, now2);
	}

	@Test
	void save_shouldPersistHistory() {
		// when
		service.save(MEMBER_ID, VIDEO_ID1);

		// then: repository.save() 호출과 전달된 엔티티 검사
		then(repository).should().save(historyCaptor.capture());
		VideoHistory saved = historyCaptor.getValue();
		assertThat(saved.getId().getMemberId()).isEqualTo(MEMBER_ID);
		assertThat(saved.getId().getVideoId()).isEqualTo(VIDEO_ID1);
		assertThat(saved.getCreatedAt()).isNotNull();
	}

	@Test
	void getRecentHistories_shouldReturnMappedDtos() {
		// given: 엔티티 생성 (createdAt은 매핑에 쓰이지 않으므로 값은 상관없음)
		VideoHistory e1 = VideoHistory.builder().memberId(MEMBER_ID).videoId(VIDEO_ID1).build();
		VideoHistory e2 = VideoHistory.builder().memberId(MEMBER_ID).videoId(VIDEO_ID2).build();

		given(repository.findTop5ByIdMemberIdOrderByCreatedAtDesc(MEMBER_ID))
			.willReturn(List.of(e1, e2));
		given(mapper.toDto(e1)).willReturn(dto1);
		given(mapper.toDto(e2)).willReturn(dto2);

		// when
		List<VideoHistoryResponse> result = service.getRecentHistories(MEMBER_ID);

		// then
		assertThat(result).containsExactly(dto1, dto2);
		then(repository).should().findTop5ByIdMemberIdOrderByCreatedAtDesc(MEMBER_ID);
		then(mapper).should(times(1)).toDto(e1);
		then(mapper).should(times(1)).toDto(e2);
	}

	@Test
	void getAllHistories_shouldReturnMappedDtos() {
		// given
		VideoHistory e1 = VideoHistory.builder().memberId(MEMBER_ID).videoId(VIDEO_ID1).build();
		VideoHistory e2 = VideoHistory.builder().memberId(MEMBER_ID).videoId(VIDEO_ID2).build();

		given(repository.findAllByIdMemberIdOrderByCreatedAtDesc(MEMBER_ID))
			.willReturn(List.of(e2, e1));
		given(mapper.toDto(e2)).willReturn(dto2);
		given(mapper.toDto(e1)).willReturn(dto1);

		// when
		List<VideoHistoryResponse> result = service.getAllHistories(MEMBER_ID);

		// then
		assertThat(result).containsExactly(dto2, dto1);
		then(repository).should().findAllByIdMemberIdOrderByCreatedAtDesc(MEMBER_ID);
		then(mapper).should(times(1)).toDto(e2);
		then(mapper).should(times(1)).toDto(e1);
	}
}
