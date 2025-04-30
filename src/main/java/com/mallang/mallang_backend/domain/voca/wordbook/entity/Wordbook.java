package com.mallang.mallang_backend.domain.voca.wordbook.entity;

import static com.mallang.mallang_backend.global.constants.AppConstants.*;
import static com.mallang.mallang_backend.global.exception.ErrorCode.*;

import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.global.common.Language;
import com.mallang.mallang_backend.global.exception.ServiceException;

import jakarta.persistence.*;
import lombok.*;

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

    public void updateName(String name) {
        if (DEFAULT_WORDBOOK_NAME.equals(this.name) || DEFAULT_WORDBOOK_NAME.equals(name)) {
            throw new ServiceException(WORDBOOK_RENAME_DEFAULT_FORBIDDEN);
        }
        this.name = name;
    }
}
