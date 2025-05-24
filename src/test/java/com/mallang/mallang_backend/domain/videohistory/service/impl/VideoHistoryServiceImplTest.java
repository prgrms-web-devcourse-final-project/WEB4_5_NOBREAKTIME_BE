package com.mallang.mallang_backend.domain.videohistory.service.impl;

import static com.mallang.mallang_backend.global.exception.ErrorCode.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.mallang.mallang_backend.domain.member.repository.MemberRepository;
import com.mallang.mallang_backend.domain.video.video.repository.VideoRepository;
import com.mallang.mallang_backend.domain.videohistory.entity.VideoHistory;
import com.mallang.mallang_backend.domain.videohistory.repository.VideoHistoryRepository;
import com.mallang.mallang_backend.global.exception.ServiceException;

class VideoHistoryServiceImplTest {

	@Mock private VideoHistoryRepository historyRepo;
	@Mock private MemberRepository memberRepo;
	@Mock private VideoRepository videoRepo;
	@InjectMocks private VideoHistoryServiceImpl service;

	private final Long MEMBER_ID = 1L;
	private final String VIDEO_ID = "vid-1";

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	@DisplayName("save: 신규 기록이면 저장하고 삭제 로직 수행 여부 확인")
	void save_newRecord() {
		var member = VideoHistoryTestFactory.createMember(MEMBER_ID);
		var video  = VideoHistoryTestFactory.createVideos(VIDEO_ID);

		when(memberRepo.findById(MEMBER_ID)).thenReturn(Optional.of(member));
		when(videoRepo.findById(VIDEO_ID)).thenReturn(Optional.of(video));
		when(historyRepo.findByMemberAndVideos(member, video)).thenReturn(Optional.empty());
		when(historyRepo.countByMember(member)).thenReturn(1);

		service.save(MEMBER_ID, VIDEO_ID);

		verify(historyRepo).save(any(VideoHistory.class));
		// 1개만 있어서 삭제 로직은 호출되지 않아야 함
		verify(historyRepo, never()).deleteAllInBatch(anyList());
	}

	@Test
	@DisplayName("save: 기존 기록이면 timestamp만 업데이트")
	void save_existingRecord() {
		var member  = VideoHistoryTestFactory.createMember(MEMBER_ID);
		var video   = VideoHistoryTestFactory.createVideos(VIDEO_ID);
		var history = spy(VideoHistoryTestFactory.createVideoHistory(
			10L, member, video, LocalDateTime.now().minusDays(1))
		);

		when(memberRepo.findById(MEMBER_ID)).thenReturn(Optional.of(member));
		when(videoRepo.findById(VIDEO_ID)).thenReturn(Optional.of(video));
		when(historyRepo.findByMemberAndVideos(member, video)).thenReturn(Optional.of(history));
		when(historyRepo.countByMember(member)).thenReturn(1);

		LocalDateTime before = history.getLastViewedAt();
		service.save(MEMBER_ID, VIDEO_ID);
		LocalDateTime after = history.getLastViewedAt();

		assertTrue(after.isAfter(before));
		verify(historyRepo, never()).save(any(VideoHistory.class));
	}

	@Test
	@DisplayName("getRecentHistories: 존재하지 않는 회원이면 예외")
	void getRecentHistories_memberNotFound() {
		when(memberRepo.findById(MEMBER_ID)).thenReturn(Optional.empty());
		ServiceException ex = assertThrows(ServiceException.class,
			() -> service.getRecentHistories(MEMBER_ID));
		assertEquals(MEMBER_NOT_FOUND, ex.getErrorCode());
	}

	@Test
	@DisplayName("getRecentHistories: 정상 조회")
	void getRecentHistories_success() {
		var member = VideoHistoryTestFactory.createMember(MEMBER_ID);
		var videos = spy(VideoHistoryTestFactory.createVideos("v1"));
		doReturn("PT2M3S").when(videos).getDuration();
		var h1 = VideoHistoryTestFactory.createVideoHistory(
			1L, member, videos, LocalDateTime.now());

		when(memberRepo.findById(MEMBER_ID)).thenReturn(Optional.of(member));
		when(historyRepo.findTop5ByMemberOrderByLastViewedAtDesc(member))
			.thenReturn(List.of(h1));

		var list = service.getRecentHistories(MEMBER_ID);
		assertEquals(1, list.size());
		assertEquals("v1", list.get(0).getVideoId());
		assertEquals("02:03", list.get(0).getDuration());
	}

	@Test
	@DisplayName("getAllHistories: 정상 조회")
	void getAllHistories_success() {
		var member = VideoHistoryTestFactory.createMember(MEMBER_ID);
		var videos = spy(VideoHistoryTestFactory.createVideos("v2"));
		doReturn("PT1H2M3S").when(videos).getDuration();
		var h2 = VideoHistoryTestFactory.createVideoHistory(
			6L, member, videos, LocalDateTime.now());

		when(memberRepo.findById(MEMBER_ID)).thenReturn(Optional.of(member));
		when(historyRepo.findTop50ByMemberOrderByLastViewedAtDesc(member))
			.thenReturn(List.of(h2));

		var list = service.getAllHistories(MEMBER_ID);
		assertEquals(1, list.size());
		assertEquals("v2", list.get(0).getVideoId());
		assertEquals("1:02:03", list.get(0).getDuration());
	}
}
