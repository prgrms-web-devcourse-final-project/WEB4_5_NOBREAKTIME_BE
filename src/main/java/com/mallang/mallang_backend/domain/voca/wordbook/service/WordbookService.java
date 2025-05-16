package com.mallang.mallang_backend.domain.voca.wordbook.service;

import java.util.List;

import com.mallang.mallang_backend.domain.voca.wordbook.dto.AddWordRequest;
import com.mallang.mallang_backend.domain.voca.wordbook.dto.AddWordToWordbookListRequest;
import com.mallang.mallang_backend.domain.voca.wordbook.dto.WordDeleteRequest;
import com.mallang.mallang_backend.domain.voca.wordbook.dto.WordMoveRequest;
import com.mallang.mallang_backend.domain.voca.wordbook.dto.WordResponse;
import com.mallang.mallang_backend.domain.voca.wordbook.dto.WordbookCreateRequest;
import com.mallang.mallang_backend.domain.voca.wordbook.dto.WordbookResponse;

public interface WordbookService {
	/**
	 * 단어장에 1개 이상의 단어를 추가합니다.
	 * @param wordbookId 단어를 추가할 단어장 ID
	 * @param request 추가할 1개 이상의 단어 DTO
	 * @param memberId 로그인한 회원 ID
	 */
	void addWords(Long wordbookId, AddWordToWordbookListRequest request, Long memberId);

	/**
	 * 단어장에 사용자 설정 언어를 추가합니다.
	 * @param wordbookId 단어를 추가할 단어장 ID
	 * @param request 추가할 단어 DTO
	 * @param memberId 로그인한 회원 ID
	 */
	void addWordCustom(Long wordbookId, AddWordRequest request, Long memberId);

	/**
	 * 추가 단어장을 생성합니다. Standard 이상 등급의 회원만 추가 단어장을 생성할 수 있습니다.
	 * @param request 생성할 추가 단어장의 이름 DTO
	 * @param memberId 로그인한 회원 ID
	 * @return 생성된 추가 단어장 ID
	 */
	Long createWordbook(WordbookCreateRequest request, Long memberId);

	/**
	 * 추가 단어장의 이름을 변경합니다. '기본 단어장'의 이름은 변경할 수 없습니다.
	 * <BR>
	 * 단어장의 이름을 '기본 단어장'으로 변경할 수 없습니다.
	 * @param wordbookId 이름을 변경할 추가 단어장 ID
	 * @param name 새로운 추가 단어장 이름
	 * @param memberId 로그인한 회원 ID
	 */
	void renameWordbook(Long wordbookId, String name, Long memberId);

	/**
	 * 추가 단어장을 삭제합니다. '기본' 단어장은 삭제할 수 없습니다.
	 * @param wordbookId 삭제할 추가 단어장 ID
	 * @param memberId 로그인한 회원 ID
	 */
	void deleteWordbook(Long wordbookId, Long memberId);

	/**
	 * 1개 이상의 단어를 다른 단어장으로 이동시킵니다.
	 * @param request 각 이동시킬 단어의 단어와 기존 단어장, 목적지 단어장 DTO
	 * @param memberId 로그인한 회원 ID
	 */
	void moveWords(WordMoveRequest request, Long memberId);

	/**
	 * 단어장에서 1개 이상의 단어를 제거합니다.
	 * @param request 제거할 단어의 각 단어장 ID, 단어
	 * @param memberId 로그인한 회원 ID
	 */
	void deleteWords(WordDeleteRequest request, Long memberId);

	/**
	 * 단어장에 속한 단어들을 무작위 순서로 반환합니다.
	 * <BR>
	 * '단어장 학습'의 카드 학습에 사용됩니다.
	 * @param wordbookId 단어장 ID
	 * @param memberId 로그인한 회원 ID
	 * @return 무작위 순서의 단어장 단어
	 */
	List<WordResponse> getWordsRandomly(Long wordbookId, Long memberId);

	/**
	 * 자신의 단어장 목록을 조회합니다.
	 * @param memberId 로그인한 회원 ID
	 * @return 회원의 단어장 목록
	 */
	List<WordbookResponse> getWordbooks(Long memberId);

	/**
	 * 단어장 페이지에 접속했을 때, 선택한 단어장의 단어들을 조회합니다.
	 * <BR>
	 * 선택한 단어장이 없으면 "기본" 단어장의 단어를 조회합니다.
	 * @param wordbookIds 단어장 ID 리스트
	 * @param userDetail 로그인한 회원
	 * @return 단어 리스트
	 */
	List<WordResponse> getWordbookItems(List<Long> wordbookIds, Long memberId);
}
