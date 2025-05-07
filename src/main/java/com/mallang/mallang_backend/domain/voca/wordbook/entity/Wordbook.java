package com.mallang.mallang_backend.domain.voca.wordbook.entity;

import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.global.common.Language;
import com.mallang.mallang_backend.global.exception.ServiceException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.ArrayList;
import java.util.List;

import static com.mallang.mallang_backend.global.constants.AppConstants.DEFAULT_WORDBOOK_NAME;
import static com.mallang.mallang_backend.global.exception.ErrorCode.WORDBOOK_RENAME_DEFAULT_FORBIDDEN;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Wordbook {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "wordbook_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id")
	@OnDelete(action = OnDeleteAction.CASCADE)
	private Member member;

	@Column(nullable = false)
	private String name;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Language language;

	@Builder
	public Wordbook(
		Member member,
		String name,
		Language language
	) {
		this.member = member;
		this.name = name;
		this.language = language;
	}

	/**
	 * 모든 언어에 대해 기본 단어장을 생성합니다.
	 * @param member 회원가입한 회원
	 * @return 생성된 기본 단어장 리스트
	 */
	public static List<Wordbook> createDefault(Member member) {
		List<Wordbook> defaultWordbooks = new ArrayList<>();
		for (Language language : Language.values()) {
			if (language == Language.NONE) {
				continue;
			}
			defaultWordbooks.add(new Wordbook(
				member,
				DEFAULT_WORDBOOK_NAME,
				language
			));
		}

		return defaultWordbooks;
	}

	/**
	 * 추가 단어장의 이름을 변경합니다. 변경하려는 단어장의 기존 이름이 '기본'이거나 변경하려는 이름이 '기본'이면 실패합니다.
	 * @param name 변경하려는 단어장 이름
	 */
	public void updateName(String name) {
		if (DEFAULT_WORDBOOK_NAME.equals(this.name) || DEFAULT_WORDBOOK_NAME.equals(name)) {
			throw new ServiceException(WORDBOOK_RENAME_DEFAULT_FORBIDDEN);
		}
		this.name = name;
	}
}
