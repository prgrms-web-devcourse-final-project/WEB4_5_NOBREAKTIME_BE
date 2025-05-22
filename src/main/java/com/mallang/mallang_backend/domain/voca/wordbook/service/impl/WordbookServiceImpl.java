package com.mallang.mallang_backend.domain.voca.wordbook.service.impl;

import com.mallang.mallang_backend.domain.member.entity.Member;
import com.mallang.mallang_backend.domain.member.repository.MemberRepository;
import com.mallang.mallang_backend.domain.quiz.wordquizresult.repository.WordQuizResultRepository;
import com.mallang.mallang_backend.domain.video.subtitle.entity.Subtitle;
import com.mallang.mallang_backend.domain.video.subtitle.repository.SubtitleRepository;
import com.mallang.mallang_backend.domain.video.video.entity.Videos;
import com.mallang.mallang_backend.domain.video.video.repository.VideoRepository;
import com.mallang.mallang_backend.domain.voca.word.entity.Word;
import com.mallang.mallang_backend.domain.voca.word.repository.WordRepository;
import com.mallang.mallang_backend.domain.voca.word.service.impl.SavedWordResultFetcher;
import com.mallang.mallang_backend.domain.voca.wordbook.dto.*;
import com.mallang.mallang_backend.domain.voca.wordbook.entity.Wordbook;
import com.mallang.mallang_backend.domain.voca.wordbook.repository.WordbookRepository;
import com.mallang.mallang_backend.domain.voca.wordbook.service.WordbookService;
import com.mallang.mallang_backend.domain.voca.wordbookitem.entity.WordbookItem;
import com.mallang.mallang_backend.domain.voca.wordbookitem.repository.WordbookItemRepository;
import com.mallang.mallang_backend.global.common.Language;
import com.mallang.mallang_backend.global.exception.ErrorCode;
import com.mallang.mallang_backend.global.exception.ServiceException;
import com.mallang.mallang_backend.global.gpt.service.GptService;
import com.mallang.mallang_backend.global.util.redis.RedisDistributedLock;
import com.mallang.mallang_backend.global.validation.WordValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.mallang.mallang_backend.global.constants.AppConstants.DEFAULT_WORDBOOK_NAME;
import static com.mallang.mallang_backend.global.exception.ErrorCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WordbookServiceImpl implements WordbookService {

    private final WordbookRepository wordbookRepository;
    private final WordRepository wordRepository;
    private final WordbookItemRepository wordbookItemRepository;
    private final MemberRepository memberRepository;
    private final SubtitleRepository subtitleRepository;
    private final GptService gptService;
    private final WordQuizResultRepository wordQuizResultRepository;
    private final RedisDistributedLock redisDistributedLock;
    private final SavedWordResultFetcher savedWordResultFetcher;
    private final VideoRepository videoRepository;

    // 단어장에 단어 추가
    @Transactional
    @Override
    public void addWords(Long wordbookId, AddWordToWordbookListRequest request, Long memberId) {
        // 단어장 존재 + 권한 체크
        Wordbook wordbook = wordbookRepository.findByIdAndMemberId(wordbookId, memberId)
                .orElseThrow(() -> new ServiceException(NO_WORDBOOK_EXIST_OR_FORBIDDEN));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ServiceException(MEMBER_NOT_FOUND));

        // 추가 단어장 사용 권한이 없으면 추가 단어장에 단어 추가 실패
        if (!member.canUseAdditaional() && !Wordbook.isDefault(wordbook)) {
            throw new ServiceException(NO_PERMISSION);
        }

        // 단어가 사용자의 설정 언어와 일치하는지 검사
        boolean hasMismatch = request.getWords().stream()
                .map(w -> w.getWord())
                .anyMatch(word -> !WordValidator.isLanguageMatch(word, member.getLanguage()));
        if (hasMismatch) {
            throw new ServiceException(LANGUAGE_MISMATCH);
        }

        for (AddWordToWordbookRequest dto : request.getWords()) {
            // 저장된 단어가 없는 경우, 사전 API 또는 GPT 처리해서 word 추가 (일반적인 경우엔 단어가 이미 존재함)
            try {
                saveWordIfNotExist(dto.getWord(), member.getLanguage());
            } catch (ServiceException e) {
                log.warn("단어 저장 실패 : {}", dto.getWord(), e);
                continue;
            }

            // 단어가 단어장에 저장되어 있지 않을 때만 저장
            if (wordbookItemRepository.findByWordbookIdAndWord(wordbook.getId(), dto.getWord()).isEmpty()) {

                // WordbookItem 생성 및 저장
                WordbookItem item = WordbookItem.builder()
                        .wordbook(wordbook)
                        .word(dto.getWord())
                        .subtitleId(dto.getSubtitleId())
                        .videoId(dto.getVideoId())
                        .build();

                try {
                    wordbookItemRepository.save(item);
                } catch (DataIntegrityViolationException ex) {
                    // 이미 다른 트랜잭션이 삽입 완료 → 무시
                }
            }
        }
    }

    // 단어장에 커스텀 단어 추가
    @Transactional
    @Override
    public void addWordCustom(Long wordbookId, AddWordRequest request, Long memberId) {
        // 단어장 존재 + 권한 체크
        Wordbook wordbook = wordbookRepository.findByIdAndMemberId(wordbookId, memberId)
                .orElseThrow(() -> new ServiceException(NO_WORDBOOK_EXIST_OR_FORBIDDEN));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ServiceException(MEMBER_NOT_FOUND));

        // 추가 단어장 사용 권한이 없으면 추가 단어장에 단어 추가 실패
        if (!Wordbook.isDefault(wordbook) && !member.canUseAdditaional()) {
            throw new ServiceException(NO_PERMISSION);
        }

        String word = request.getWord();

        if (!WordValidator.isLanguageMatch(word, member.getLanguage())) {
            throw new ServiceException(LANGUAGE_MISMATCH);
        }

        // 저장된 단어가 없는 경우, 사전 API 또는 GPT 처리해서 word 추가 (일반적인 경우엔 단어가 이미 존재함)
        saveWordIfNotExist(word, member.getLanguage());

        // 단어가 단어장에 저장되어 있지 않을 때만 저장
        if (wordbookItemRepository.findByWordbookIdAndWord(wordbook.getId(), word).isEmpty()) {

            // WordbookItem 생성 및 저장
            WordbookItem item = WordbookItem.builder()
                    .wordbook(wordbook)
                    .word(word)
                    .subtitleId(null)
                    .videoId(null)
                    .build();


            try {
                wordbookItemRepository.save(item);
            } catch (DataIntegrityViolationException ex) {
                // 이미 다른 트랜잭션이 삽입 완료 → 무시
            }
            return;
        }
        throw new ServiceException(DUPLICATE_WORD_SAVED);
    }

    /**
     * 단어가 WordRepository에 저장되어 있지 않으면 GPT 호출로 단어를 검색하고, WordRepository에 저장합니다.
     *
     * @param word 저장되어야 하는 단어
     */
    private void saveWordIfNotExist(String word, Language language) {
        List<Word> words = wordRepository.findByWord(word); // DB 조회
        if (words.isEmpty()) {
            // 락 획득 시도
            String lockKey = "lock:word:saved:" + word;
            String lockValue = UUID.randomUUID().toString();
            long ttlMillis = Duration.ofMinutes(1).toMillis();

            boolean locked = redisDistributedLock.tryLock(lockKey, lockValue, ttlMillis);
            if (!locked) {
                // 락이 사라졌는지 1분간 계속 확인
                boolean lockAvailable = redisDistributedLock.waitForUnlockThenFetch(lockKey, ttlMillis, 1000L);
                // 최대 재시도 시간까지 확인했으나 실패함
                if (!lockAvailable) {
                    throw new ServiceException(SAVED_WORD_CONCURRENCY_TIME_OUT);
                }
                // 락이 사라졌으면 다른 작업으로 처리된 결과를 DB에서 찾아서 응답
                words = savedWordResultFetcher.fetchSavedWordResultAfterWait(word);
                if (words.isEmpty()) {
                    throw new ServiceException(WORD_PARSE_FAILED);
                }
                return;
            }

            try {
                List<Word> generatedWords = gptService.searchWord(word, language); // DB에 없으면 GPT 호출
                wordRepository.saveAll(generatedWords);

            } finally {
                redisDistributedLock.unlock(lockKey, lockValue);
            }
        }
    }

    // 추가 단어장 생성
    @Transactional
    @Override
    public Long createWordbook(WordbookCreateRequest request, Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ServiceException(MEMBER_NOT_FOUND));

        if (member.getLanguage() == Language.NONE) {
            throw new ServiceException(LANGUAGE_IS_NONE);
        }

        if (request.getName().equals(DEFAULT_WORDBOOK_NAME)) {
            throw new ServiceException(WORDBOOK_CREATE_DEFAULT_FORBIDDEN);
        }

        if (wordbookRepository.existsByMemberAndName(member, request.getName())) {
            throw new ServiceException(DUPLICATE_WORDBOOK_NAME);
        }

        Wordbook wordbook = Wordbook.builder()
                .member(member)
                .name(request.getName())
                .language(member.getLanguage())
                .build();

        return wordbookRepository.save(wordbook).getId();
    }

    // 추가 단어장 이름 변경
    @Transactional
    @Override
    public void renameWordbook(Long wordbookId, String name, Long memberId) {
        Wordbook wordbook = wordbookRepository.findByIdAndMemberId(wordbookId, memberId)
                .orElseThrow(() -> new ServiceException(NO_WORDBOOK_EXIST_OR_FORBIDDEN));

        wordbook.updateName(name);
    }

    // 추가 단어장 삭제
    @Transactional
    @Override
    public void deleteWordbook(Long wordbookId, Long memberId) {
        Wordbook wordbook = wordbookRepository.findByIdAndMemberId(wordbookId, memberId)
                .orElseThrow(() -> new ServiceException(NO_WORDBOOK_EXIST_OR_FORBIDDEN));

        if (DEFAULT_WORDBOOK_NAME.equals(wordbook.getName())) {
            throw new ServiceException(WORDBOOK_DELETE_DEFAULT_FORBIDDEN);
        }

        // 삭제되는 단어와 관련된 퀴즈 결과 삭제
        List<WordbookItem> items = wordbookItemRepository.findAllByWordbook(wordbook);
        for (WordbookItem item : items) {
            wordQuizResultRepository.deleteAllByWordbookItem(item);
        }
        // 추가 단어장 삭제 시 들어있는 단어 아이템들도 삭제
        wordbookItemRepository.deleteAllByWordbookId(wordbookId);
        wordbookRepository.delete(wordbook);
    }

    // 단어장의 단어 이동
    @Transactional
    @Override
    public void moveWords(WordMoveRequest request, Long memberId) {
        Long toId = request.getDestinationWordbookId();

        Wordbook toWordbook = wordbookRepository.findByIdAndMemberId(toId, memberId)
                .orElseThrow(() -> new ServiceException(NO_WORDBOOK_EXIST_OR_FORBIDDEN));

        for (WordMoveItem item : request.getWords()) {
            // 같은 단어장으로 이동하려고 하면 통과
            if (toId.equals(item.getFromWordbookId())) {
                continue;
            }

            // 기존 단어장 조회
            Wordbook fromWordbook = wordbookRepository.findByIdAndMemberId(item.getFromWordbookId(), memberId)
                    .orElseThrow(() -> new ServiceException(NO_WORDBOOK_EXIST_OR_FORBIDDEN));

            // 기존 WordbookItem 찾기
            WordbookItem existingItem = wordbookItemRepository.findByWordbookAndWord(fromWordbook, item.getWord())
                    .orElseThrow(() -> new ServiceException(WORDBOOK_ITEM_NOT_FOUND));

            existingItem.updateWordbook(toWordbook);
            wordbookItemRepository.save(existingItem);
        }
    }

    // 단어장에서 단어 삭제
    @Transactional
    @Override
    public void deleteWords(WordDeleteRequest request, Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ServiceException(MEMBER_NOT_FOUND));

        for (WordDeleteItem item : request.getWords()) {
            // 단어장 조회 및 권한 체크
            Wordbook wordbook = wordbookRepository.findByIdAndMemberId(item.getWordbookId(), memberId)
                    .orElseThrow(() -> new ServiceException(NO_WORDBOOK_EXIST_OR_FORBIDDEN));

            // 추가 단어장 사용 권한이 없으면 추가 단어장의 단어 삭제 불가능
            if (!Wordbook.isDefault(wordbook) && !member.canUseAdditaional()) {
                throw new ServiceException(ErrorCode.NO_PERMISSION);
            }

            // WordbookItem 조회
            WordbookItem itemToDelete = wordbookItemRepository.findByWordbookAndWord(wordbook, item.getWord())
                    .orElseThrow(() -> new ServiceException(WORDBOOK_ITEM_NOT_FOUND));

            // 퀴즈 결과에서 단어와 관련된 퀴즈 결과 삭제
            wordQuizResultRepository.deleteAllByWordbookItem(itemToDelete);

            // 단어장 단어 삭제
            wordbookItemRepository.delete(itemToDelete);
        }
    }

    // 단어장에 단어들 조회(랜덤 순서)
    @Override
    public List<WordResponse> getWordsRandomly(Long wordbookId, Long memberId) {
        // 단어장 존재 여부 및 권한 확인
        Wordbook wordbook = wordbookRepository.findByIdAndMemberId(wordbookId, memberId)
                .orElseThrow(() -> new ServiceException(NO_WORDBOOK_EXIST_OR_FORBIDDEN));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ServiceException(MEMBER_NOT_FOUND));

        // 추가 단어장 사용 권한이 없으면 추가 단어장의 단어 조회 불가능
        if (!Wordbook.isDefault(wordbook) && !member.canUseAdditaional()) {
            throw new ServiceException(ErrorCode.NO_PERMISSION);
        }

        // 단어장 아이템 조회
        List<WordbookItem> items = wordbookItemRepository.findAllByWordbook(wordbook);

        // 모든 단어명 추출
        List<WordResponse> result = convertToWordResponses(items);
        Collections.shuffle(result);
        return result;
    }

    // 단어장 조회
    @Override
    public List<WordbookResponse> getWordbooks(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ServiceException(MEMBER_NOT_FOUND));

        List<Wordbook> wordbooks = wordbookRepository.findAllByMemberIdAndLanguage(memberId, member.getLanguage());

        // 추가 단어장 사용 권한이 없으면 기본 단어장만 응답
        if (!member.canUseAdditaional()) {
            wordbooks = wordbooks.stream()
                    .filter(wb -> Wordbook.isDefault(wb))
                    .toList();
        }

        return wordbooks.stream()
                .map(w -> new WordbookResponse(
                        w.getId(),
                        w.getName(),
                        w.getLanguage(),
                        wordbookItemRepository.countByWordbook(w),
                        wordbookItemRepository.countByWordbookAndLearnedTrue(w)
                )).toList();
    }

    @Override
    public List<WordResponse> getWordbookItems(List<Long> wordbookIds, Long memberId) {
        // 사용자 인증
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ServiceException(MEMBER_NOT_FOUND));

        validateWordbookIdsExist(wordbookIds);

        List<WordbookItem> items = findWordbookItems(wordbookIds, member);

        return convertToWordResponses(items);
    }

    private List<WordbookItem> findWordbookItems(List<Long> wordbookIds, Member member) {

        // 기본 단어장 조회 - 단어장 Id가 비어있거나 null인 경우
        if (wordbookIds == null || wordbookIds.isEmpty()) {
            Wordbook wordbook = wordbookRepository.findByMemberAndNameAndLanguage(member, DEFAULT_WORDBOOK_NAME, member.getLanguage())
                    .orElseThrow(() -> new ServiceException(NO_WORDBOOK_EXIST_OR_FORBIDDEN));

            return wordbookItemRepository.findAllByWordbookOrderByCreatedAtDesc(wordbook);
        }

        // 단일 단어장 조회 - 단어장 Id가 1개인 경우
        if (wordbookIds.size() == 1) {
            Wordbook wordbook = wordbookRepository.findById(wordbookIds.get(0))
                    .orElseThrow(() -> new ServiceException(NO_WORDBOOK_EXIST_OR_FORBIDDEN));

            if (!wordbook.getMember().getId().equals(member.getId())) {
                throw new ServiceException(NO_WORDBOOK_EXIST_OR_FORBIDDEN);
            }

            return wordbookItemRepository.findAllByWordbookOrderByCreatedAtDesc(wordbook);
        }

        // 여러 단어장 조회 - 단어장 Id가 2개 이상인 경우
        List<Wordbook> wordbooks = wordbookRepository.findAllById(wordbookIds);

        // 단어장 소유자 확인
        if (wordbooks.stream().anyMatch(wb -> !wb.getMember().getId().equals(member.getId()))) {
            throw new ServiceException(NO_WORDBOOK_EXIST_OR_FORBIDDEN);
        }

        return wordbookItemRepository.findAllByWordbookIdInOrderByCreatedAtDesc(wordbookIds);
    }

    private void validateWordbookIdsExist(List<Long> requestedIds) {
        // 단어장 ID가 비어있거나 null인 경우 기본 단어장 조회
        if (requestedIds == null || requestedIds.isEmpty()) {
            return; // 기본 단어장 케이스는 통과
        }

        // 실제 존재하는 ID 조회
        Set<Long> existingIdSet = wordbookRepository.findAllById(requestedIds).stream()
                .map(Wordbook::getId)
                .collect(Collectors.toSet());

        // 존재하지 않는 ID 추출
        List<Long> invalidIds = requestedIds.stream()
                .filter(id -> !existingIdSet.contains(id))
                .toList();

        if (!invalidIds.isEmpty()) {
            throw new ServiceException(NO_WORDBOOK_EXIST_OR_FORBIDDEN);
        }
    }

    private List<WordResponse> convertToWordResponses(List<WordbookItem> items) {
        // 모든 단어명 추출
        List<String> wordList = items.stream()
                .map(WordbookItem::getWord)
                .collect(Collectors.toList());

        // Word 테이블에서 일괄 조회 (단어명 중복 허용)
        List<Word> wordEntities = wordRepository.findByWordIn(wordList);

        // 먼저 등장한 단어만 Map에 저장 (중복 제거)
        Map<String, Word> wordMap = new LinkedHashMap<>();
        for (Word wordEntity : wordEntities) {
            wordMap.putIfAbsent(wordEntity.getWord(), wordEntity);
        }

        // Subtitle 엔티티 조회 (subtitleId가 null이 아닌 경우만)
        List<Long> subtitleIds = items.stream()
                .map(WordbookItem::getSubtitleId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        Map<Long, Subtitle> subtitleMap = subtitleIds.isEmpty() ?
                Collections.emptyMap() :
                subtitleRepository.findByIdIn(subtitleIds).stream()
                        .collect(Collectors.toMap(Subtitle::getId, Function.identity()));

        // Subtitle 엔티티 조회 (subtitleId가 null이 아닌 경우만)
        List<String> videoIds = items.stream()
                .map(WordbookItem::getVideoId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        Map<String, Videos> videoMap = videoIds.isEmpty() ?
                Collections.emptyMap() :
                videoRepository.findByIdIn(videoIds).stream()
                        .collect(Collectors.toMap(Videos::getId, Function.identity()));

        // 응답 생성
        return items.stream()
                .map(item -> {
                    Word wordEntity = wordMap.get(item.getWord());
                    if (wordEntity == null) return null;

                    String exampleSentence = wordEntity.getExampleSentence();
                    String translatedSentence = wordEntity.getTranslatedSentence();

                    if (item.getSubtitleId() != null && subtitleMap.containsKey(item.getSubtitleId())) {
                        Subtitle subtitle = subtitleMap.get(item.getSubtitleId());
                        exampleSentence = subtitle.getOriginalSentence();
                        translatedSentence = subtitle.getTranslatedSentence();
                    }

                    String videoTitle = null;
                    String imageUrl = null;

                    if (item.getVideoId() != null && videoMap.containsKey(item.getVideoId())) {
                        Videos videos = videoMap.get(item.getVideoId());
                        videoTitle = videos.getVideoTitle();
                        imageUrl = videos.getThumbnailImageUrl();
                    }

                    return new WordResponse(
                            item.getWord(),
                            wordEntity.getPos(),
                            wordEntity.getMeaning(),
                            wordEntity.getDifficulty().toString(),
                            exampleSentence,
                            translatedSentence,
                            item.getVideoId(),
                            videoTitle,
                            imageUrl,
                            item.getSubtitleId(),
                            item.getCreatedAt(),
                            item.getWordbook().getId()
                    );
                })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(WordResponse::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }
}
