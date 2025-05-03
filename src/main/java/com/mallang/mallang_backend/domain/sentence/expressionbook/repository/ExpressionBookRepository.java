package com.mallang.mallang_backend.domain.sentence.expressionbook.repository;

import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.sentence.expressionbook.entity.ExpressionBook;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExpressionBookRepository extends JpaRepository<ExpressionBook, Long> {
    List<ExpressionBook> findAllByMember(Member member);

	Optional<ExpressionBook> findByIdAndMember(Long expressionBookId, Member member);
}
