package com.mallang.mallang_backend.domain.videohistory.service.impl;

import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.member.repository.MemberRepository;
import com.mallang.mallang_backend.domain.video.video.entity.Videos;
import com.mallang.mallang_backend.domain.video.video.repository.VideoRepository;
import com.mallang.mallang_backend.domain.videohistory.dto.VideoHistoryResponse;
import com.mallang.mallang_backend.domain.videohistory.entity.VideoHistory;
import com.mallang.mallang_backend.domain.videohistory.repository.VideoHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class VideoHistoryServiceImplTest {

	@Mock private VideoHistoryRepository repository;
	@Mock private MemberRepository memberRepository;
	@Mock private VideoRepository videoRepository;
	@InjectMocks private VideoHistoryServiceImpl service;

	@Captor private ArgumentCaptor<VideoHistory> historyCaptor;
	@Captor private ArgumentCaptor<List<VideoHistory>> deleteCaptor;

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
		member  = mock(Member.class);
		videos1 = mock(Videos.class);
		videos2 = mock(Videos.class);

		dto1 = new VideoHistoryResponse(
			VIDEO_ID1, TITLE1, THUMB1,
			LocalDateTime.of(2025,4,29,10,0)
		);
		dto2 = new VideoHistoryResponse(
			VIDEO_ID2, TITLE2, THUMB2,
			LocalDateTime.of(2025,4,28,9,30)
		);
	}

	@Test @DisplayName("save() 호출 시 VideoHistory 엔티티가 정상 저장된다")
	void save_shouldPersistHistory() {
		given(memberRepository.findById(MEMBER_ID))
			.willReturn(Optional.of(member));
		given(videoRepository.findById(VIDEO_ID1))
			.willReturn(Optional.of(videos1));
		given(repository.findByMemberAndVideos(member, videos1))
			.willReturn(Optional.empty());
		// 삭제 로직이 실행되지 않도록
		given(repository.countByMember(member)).willReturn(0);

		service.save(MEMBER_ID, VIDEO_ID1);

		then(repository).should().save(historyCaptor.capture());
		VideoHistory saved = historyCaptor.getValue();

		// 수동으로 createdAt, lastViewedAt 설정 (Mockito에서는 실제 persist가 안되므로 직접 세팅)
		saved.setCreatedAt(LocalDateTime.now());
		saved.updateTimestamp();

		assertThat(saved.getMember()).isEqualTo(member);
		assertThat(saved.getVideos()).isEqualTo(videos1);
		assertThat(saved.getCreatedAt()).isNotNull();
		assertThat(saved.getLastViewedAt()).isEqualTo(saved.getCreatedAt());

		then(repository).should(never()).deleteAllInBatch(anyList());
	}

	@Test @DisplayName("getRecentHistories() 호출 시 최신 5개 DTO 반환")
	void getRecentHistories_shouldReturnMappedDtos() throws Exception {
		given(memberRepository.findById(MEMBER_ID))
			.willReturn(Optional.of(member));

		// 엔티티 생성 및 lastViewedAt 설정
		VideoHistory e1 = VideoHistory.builder().member(member).videos(videos1).build();
		VideoHistory e2 = VideoHistory.builder().member(member).videos(videos2).build();
		Field fv = VideoHistory.class.getDeclaredField("lastViewedAt");
		fv.setAccessible(true);
		fv.set(e1, dto1.getLastViewedAt());
		fv.set(e2, dto2.getLastViewedAt());

		// Videos 엔티티 getter 모킹
		given(videos1.getId()).willReturn(VIDEO_ID1);
		given(videos1.getVideoTitle()).willReturn(TITLE1);
		given(videos1.getThumbnailImageUrl()).willReturn(THUMB1);
		given(videos2.getId()).willReturn(VIDEO_ID2);
		given(videos2.getVideoTitle()).willReturn(TITLE2);
		given(videos2.getThumbnailImageUrl()).willReturn(THUMB2);

		given(repository.findTop5ByMemberOrderByLastViewedAtDesc(member))
			.willReturn(List.of(e1, e2));

		List<VideoHistoryResponse> result = service.getRecentHistories(MEMBER_ID);

		then(repository).should().findTop5ByMemberOrderByLastViewedAtDesc(member);
		assertThat(result).hasSize(2)
			.extracting("videoId", "lastViewedAt")
			.containsExactly(
				tuple(VIDEO_ID1, dto1.getLastViewedAt()),
				tuple(VIDEO_ID2, dto2.getLastViewedAt())
			);
	}

	@Test @DisplayName("getAllHistories() 호출 시 전체 기록을 DTO로 변환하여 반환한다")
	void getAllHistories_shouldReturnMappedDtos() throws Exception {
		given(memberRepository.findById(MEMBER_ID))
			.willReturn(Optional.of(member));

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

		// 전체 내림차순(e2 먼저, e1 나중)
		given(repository.findAllByMemberOrderByLastViewedAtDesc(member))
			.willReturn(List.of(e2, e1));

		List<VideoHistoryResponse> result = service.getAllHistories(MEMBER_ID);

		then(repository).should().findAllByMemberOrderByLastViewedAtDesc(member);
		assertThat(result).hasSize(2);
		assertThat(result.get(0)).usingRecursiveComparison().isEqualTo(dto2);
		assertThat(result.get(1)).usingRecursiveComparison().isEqualTo(dto1);
	}

	@Test @DisplayName("save() 호출 시 50개 초과하면 오래된 excess개만 삭제")
	void save_shouldDeleteOnlyExcessOnes() {
		given(memberRepository.findById(MEMBER_ID))
			.willReturn(Optional.of(member));
		given(videoRepository.findById(VIDEO_ID1))
			.willReturn(Optional.of(videos1));
		given(repository.findByMemberAndVideos(member, videos1))
			.willReturn(Optional.empty());
		// 이미 52개 있다고 가정
		given(repository.countByMember(member)).willReturn(52);

		List<VideoHistory> all = IntStream.range(0, 52)
			.mapToObj(i -> mock(VideoHistory.class))
			.toList();
		given(repository.findAllByMemberOrderByLastViewedAtAsc(member))
			.willReturn(all);

		service.save(MEMBER_ID, VIDEO_ID1);

		then(repository).should().deleteAllInBatch(deleteCaptor.capture());
		List<VideoHistory> deleted = deleteCaptor.getValue();
		assertThat(deleted).hasSize(2)
			.containsExactly(all.get(0), all.get(1));
	}
}
