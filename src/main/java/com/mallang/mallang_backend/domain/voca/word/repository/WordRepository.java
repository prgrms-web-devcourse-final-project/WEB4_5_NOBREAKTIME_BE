package com.mallang.mallang_backend.domain.voca.word.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mallang.mallang_backend.domain.voca.word.entity.Word;

public interface WordRepository extends JpaRepository<Word, Long> {

    List<Word> findByWord(String word);

    List<Word> findByWordIn(List<String> words);

    Optional<Word> findFirstByWord(String word);

    Optional<Word> findFirstByWordOrderByIdAsc(String word);
}
