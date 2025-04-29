package com.mallang.mallang_backend.domain.voca.word.repository;

import com.mallang.mallang_backend.domain.voca.word.entity.Word;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WordRepository extends JpaRepository<Word, Long> {

    List<Word> findByWord(String word);
}
