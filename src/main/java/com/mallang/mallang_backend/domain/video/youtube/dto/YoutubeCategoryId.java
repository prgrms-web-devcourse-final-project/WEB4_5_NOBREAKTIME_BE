package com.mallang.mallang_backend.domain.video.youtube;

import java.util.Optional;
import java.util.stream.Stream;

import com.mallang.mallang_backend.global.exception.ErrorCode;
import com.mallang.mallang_backend.global.exception.ServiceException;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 유튜브 비디오 카테고리 ID 목록
 */
@Getter
@RequiredArgsConstructor
public enum YoutubeCategoryId {
	FILM_ANIMATION("1"),           // 영화 및 애니메이션
	AUTOS_VEHICLES("2"),           // 자동차 및 차량
	MUSIC("10"),                   // 음악
	PETS_AND_ANIMALS("15"),        // 반려동물 및 동물
	SPORTS("17"),                  // 스포츠
	SHORT_MOVIES("18"),            // 단편 영화
	TRAVEL_AND_EVENTS("19"),       // 여행 및 이벤트
	GAMING("20"),                  // 게임
	VIDEOBLOGGING("21"),           // 비디오 블로깅
	PEOPLE_AND_BLOGS("22"),        // 사람 및 블로그
	COMEDY("23"),                  // 코미디
	ENTERTAINMENT("24"),           // 엔터테인먼트
	NEWS_AND_POLITICS("25"),       // 뉴스 및 정치
	HOWTO_AND_STYLE("26"),         // 노하우 및 스타일
	EDUCATION("27"),               // 교육
	SCIENCE_AND_TECHNOLOGY("28"),  // 과학 및 기술
	NONPROFITS_AND_ACTIVISM("29"), // 비영리 및 활동
	MOVIES("30"),                  // 영화
	ANIME_AND_ANIMATION("31"),     // 애니메이션
	ACTION_AND_ADVENTURE("32"),    // 액션 및 모험
	CLASSICS("33"),                // 고전
	COMEDY_MOVIES("34"),           // 코미디 영화
	DOCUMENTARY("35"),             // 다큐멘터리
	DRAMA("36"),                   // 드라마
	FAMILY("37"),                  // 가족
	FOREIGN("38"),                 // 외국
	HORROR("39"),                  // 공포
	SCI_FI_FANTASY("40"),          // 공상 과학/판타지
	THRILLER("41"),                // 스릴러
	SHORTS("42"),                  // 쇼츠
	SHOWS("43"),                   // 쇼
	TRAILERS("44");                // 예고편

	private final String id;

	/**
	 * ID로 enum을 조회
	 */
	public static Optional<YoutubeCategoryId> fromId(String id) {
		return Stream.of(values()).filter(c -> c.id.equals(id)).findFirst();
	}

	/**
	 * ID로 enum을 조회하거나, 유효하지 않을 경우 예외 발생
	 */
	public static YoutubeCategoryId of(String id) {
		return fromId(id)
			.orElseThrow(() -> new ServiceException(
				ErrorCode.CATEGORY_NOT_FOUND));
	}
}