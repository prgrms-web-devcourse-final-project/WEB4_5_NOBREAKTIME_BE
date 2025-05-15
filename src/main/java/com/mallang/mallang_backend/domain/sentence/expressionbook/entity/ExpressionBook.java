package com.mallang.mallang_backend.domain.sentence.expressionbook.entity;

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

import static com.mallang.mallang_backend.global.constants.AppConstants.DEFAULT_EXPRESSION_BOOK_NAME;
import static com.mallang.mallang_backend.global.exception.ErrorCode.EXPRESSIONBOOK_RENAME_DEFAULT_FORBIDDEN;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExpressionBook {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "expression_book_id")
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
    public ExpressionBook(
            Member member,
            String name,
            Language language
    ) {
        this.member = member;
        this.name = name;
        this.language = language;
    }

    public static List<ExpressionBook> createDefault(Member member) {
        List<ExpressionBook> defaultExpressionbook = new ArrayList<>();
        for (Language language : Language.values()) {
            if (language == Language.NONE) continue;

            defaultExpressionbook.add(ExpressionBook.builder()
                    .member(member)
                    .name(DEFAULT_EXPRESSION_BOOK_NAME)
                    .language(language)
                    .build());
        }
        return defaultExpressionbook;
    }

    /**
     * 추가 표현함의 이름을 변경합니다. 변경하려는 표현함의 기존 이름이 '기본 표현함'이거나 변경하려는 이름이 '기본 표현함'이면 실패합니다.
     * @param newName 변경하려는 단어장 이름
     */
    public void updateName(String newName) {
        if (DEFAULT_EXPRESSION_BOOK_NAME.equals(this.name) || DEFAULT_EXPRESSION_BOOK_NAME.equals(newName)) {
            throw new ServiceException(EXPRESSIONBOOK_RENAME_DEFAULT_FORBIDDEN);
        }
        this.name = newName;
    }
}
